package us.poliscore.legiscan.service;

import java.io.File;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import us.poliscore.legiscan.cache.CachedLegiscanDatasetResult;
import us.poliscore.legiscan.cache.FileSystemLegiscanCache;
import us.poliscore.legiscan.cache.LegiscanCache;
import us.poliscore.legiscan.view.LegiscanAmendmentView;
import us.poliscore.legiscan.view.LegiscanBillTextView;
import us.poliscore.legiscan.view.LegiscanBillView;
import us.poliscore.legiscan.view.LegiscanDatasetView;
import us.poliscore.legiscan.view.LegiscanMasterListView;
import us.poliscore.legiscan.view.LegiscanMonitorView;
import us.poliscore.legiscan.view.LegiscanPeopleView;
import us.poliscore.legiscan.view.LegiscanResponse;
import us.poliscore.legiscan.view.LegiscanRollCallView;
import us.poliscore.legiscan.view.LegiscanSessionView;
import us.poliscore.legiscan.view.LegiscanSponsoredBillView;
import us.poliscore.legiscan.view.LegiscanState;
import us.poliscore.legiscan.view.LegiscanSupplementView;
import us.poliscore.legiscan.view.RefreshFrequency;

/**
 * Implements a "Legiscan Client", as defined per the Legiscan documentation. Default configuration provides for the following additional services
 * ontop of the standard Legiscan API
 * - File system caching of responses
 * - Bulk populating of datasets
 * - Updating a previously bulk populated dataset and listening to data update events
 */
public class CachedLegiscanService extends LegiscanService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachedLegiscanService.class);
    
    @Getter
    protected final LegiscanCache cache;

    protected CachedLegiscanService(String apiKey, ObjectMapper objectMapper, LegiscanCache cache) {
        super(apiKey, objectMapper);
        this.cache = cache;
    }

    public static Builder builder(String apiKey) {
        return new Builder(apiKey);
    }

    public static class Builder {
    	protected final String apiKey;
    	protected ObjectMapper objectMapper;
    	protected LegiscanCache cache;
    	protected File cacheDirectory;

        public Builder(String apiKey) {
            this.apiKey = apiKey;
        }

        public Builder withObjectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        public Builder withCache(LegiscanCache cache) {
            this.cache = cache;
            return this;
        }

        public Builder withCacheDirectory(File dir) {
            this.cacheDirectory = dir;
            return this;
        }

        public CachedLegiscanService build() {
            if (this.objectMapper == null) {
            	this.objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
            	
            	// The dataset fetching methods have some large zips which are serialized into json. Without this the deserialization will fail
            	objectMapper.getFactory().setStreamReadConstraints(StreamReadConstraints.builder().maxStringLength(100_000_000).build());
            }

            if (this.cache == null) {
                File dir = cacheDirectory != null
                        ? cacheDirectory
                        : new File(System.getProperty("user.home") + "/appdata/poliscore/legiscan");
                
                this.cache = new FileSystemLegiscanCache(dir, this.objectMapper);
            }

            var client = new CachedLegiscanService(apiKey, objectMapper, cache);
            
            return client;
        }
    }
    
    protected LegiscanResponse getOrRequest(String cacheKey, String url, ExpirationPolicy ep) {
    	val metadata = cache.peekEntry(cacheKey);
        val cached = cache.peek(cacheKey, new TypeReference<LegiscanResponse>() {});
    	
    	if (cached.isPresent() && !metadata.get().isExpired()) {
    		LOGGER.trace("Pulling object [" + cacheKey + "] from cache.");
    		return cached.get();
    	}
    	
    	LOGGER.debug("Fetching object [" + cacheKey + "] from Legiscan.");
        LegiscanResponse value = makeRequest(url);
        
        val expiration = ep.getTtl(Instant.now(), cacheKey);
        cache.put(cacheKey, value, expiration == null ? -1 : expiration.getSeconds());
        
        return value;
    }

    protected String cacheKeyFromUrl(String url) {
        try {
            URI uri = new URI(url);
            String query = uri.getRawQuery();

            Map<String, String> paramMap = new HashMap<>();
            for (String param : query.split("&")) {
                String[] pair = param.split("=", 2);
                if (pair.length == 2) {
                    String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                    if (!"key".equals(key)) {
                        paramMap.put(key, value);
                    }
                }
            }

            List<String> preferredOrder = List.of("op", "state", "year");
            List<String> orderedParts = new ArrayList<>();

            for (String key : preferredOrder) {
                if (paramMap.containsKey(key)) {
                    orderedParts.add(paramMap.remove(key));
                }
            }

            // Append remaining parameters in natural (alphabetical) order
            paramMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> orderedParts.add(entry.getValue()));

            var out = String.join("/", orderedParts).toLowerCase();
            
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Invalid URL: " + url, e);
        }
    }
    
    /**
     * Fetches the Legiscan dataset and populates the cache with the most up-to-date data. Any objects which are already cached will simply be updated.
     * The dataset's people, bills and votes can be accessed via the returned CachedLegiscanDataset.
     * 
     * @param dataset
     * @param freshness How fresh is the data? More granular specifications (i.e. hourly) will eat more Legiscan API budget (default value is WEEKLY)
     */
    @SneakyThrows
    public CachedLegiscanDatasetResult cacheDataset(LegiscanDatasetView dataset, RefreshFrequency freshness)
    {
    	var cachedDataset = new CachedLegiscanDatasetResult(this, dataset, objectMapper);
    	
    	cachedDataset.update(freshness);
    	
    	return cachedDataset;
    }
    
    public CachedLegiscanDatasetResult cacheDataset(LegiscanDatasetView dataset) { return cacheDataset(dataset, RefreshFrequency.WEEKLY); }
    
    /**
     * Fetches the regular session Legiscan dataset and populates the cache with the most up-to-date data. Any objects which are already cached will simply be updated.
     * The dataset's people, bills and votes can be accessed via the returned CachedLegiscanDataset.
     * 
     * @param state
     * @param year
     * @param freshness How fresh is the data? More granular specifications (i.e. hourly) will eat more Legiscan API budget (default value is WEEKLY)
     */
    @SneakyThrows
    public CachedLegiscanDatasetResult cacheDataset(LegiscanState state, int year, RefreshFrequency freshness) {
		List<LegiscanDatasetView> datasets = getDatasetList(state, year);
        
        for (var dataset : datasets)
        {
        	if (dataset.isSpecial() == false) {
        		return cacheDataset(dataset);
        	}
        }
        
        throw new RuntimeException("Dataset not found!");
	}
    
    public CachedLegiscanDatasetResult cacheDataset(LegiscanState state, int year) { return cacheDataset(state, year, RefreshFrequency.WEEKLY); }
    
    /**
     * Fetches the Legiscan dataset and populates the cache with the most up-to-date data. Any objects which are already cached will simply be updated.
     * The dataset's people, bills and votes can be accessed via the returned CachedLegiscanDataset.
     * 
     * @param state
     * @param year
     * @param sessionId
     * @param freshness How fresh is the data? More granular specifications (i.e. hourly) will eat more Legiscan API budget (default value is WEEKLY)
     */
    @SneakyThrows
    public CachedLegiscanDatasetResult cacheDataset(LegiscanState state, int year, int sessionId, RefreshFrequency freshness) {
		List<LegiscanDatasetView> datasets = getDatasetList(state, year);
        
        for (var dataset : datasets)
        {
        	if (dataset.getSessionId() == sessionId) {
        		return cacheDataset(dataset);
        	}
        }
        
        throw new RuntimeException("Dataset not found!");
	}
    
    public CachedLegiscanDatasetResult cacheDataset(LegiscanState state, int year, int sessionId) { return cacheDataset(state, year, sessionId, RefreshFrequency.WEEKLY); }

    @Override
    public List<LegiscanSessionView> getSessionList(LegiscanState state) {
        String url = buildUrl("getSessionList", "state", state.getAbbreviation());
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url,
                ExpirationPolicy.daily()
        );
        return response.getSessions();
    }
    
    @Override
    public LegiscanMasterListView getMasterList(int sessionId) {
        String url = buildUrl("getMasterList", "id", String.valueOf(sessionId));
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url,
                ExpirationPolicy.hourly()
        );
        return response.getMasterlist();
    }
    
    @Override
    public LegiscanMasterListView getMasterList(LegiscanState state) {
        String url = buildUrl("getMasterList", "state", state.getAbbreviation());
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url,
                ExpirationPolicy.hourly()
        );
        return response.getMasterlist();
    }
    
    @Override
    public LegiscanMasterListView getMasterListRaw(LegiscanState state) {
        String url = buildUrl("getMasterListRaw", "state", state.getAbbreviation());
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url,
                ExpirationPolicy.hourly()
        );
        return response.getMasterlist();
    }
    
    @Override
    public LegiscanMasterListView getMasterListRaw(int sessionId) {
        String url = buildUrl("getMasterListRaw", "id", String.valueOf(sessionId));
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url,
                ExpirationPolicy.hourly()
        );
        return response.getMasterlist();
    }
    
    public LegiscanBillView getBill(int billId) {
        String url = buildUrl("getBill", "id", String.valueOf(billId));
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url,
                ExpirationPolicy.fixedDuration(Duration.ofHours(3))
        );
        
        return response.getBill();
    }
    
    @Override
    public LegiscanBillTextView getBillText(int docId) {
        String url = buildUrl("getBillText", "id", String.valueOf(docId));
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url,
                ExpirationPolicy.never()
        );
        
        return response.getText();
    }
    
    @Override
    public LegiscanAmendmentView getAmendment(int amendmentId) {
        String url = buildUrl("getAmendment", "id", String.valueOf(amendmentId));
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url,
                ExpirationPolicy.never()
        );
        
        return response.getAmendment();
    }
    
    @Override
    public LegiscanSupplementView getSupplement(int supplementId) {
        String url = buildUrl("getSupplement", "id", String.valueOf(supplementId));
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url,
                ExpirationPolicy.never()
        );
        
        return response.getSupplement();
    }
    
    @Override
    public LegiscanRollCallView getRollCall(int rollCallId) {
        String url = buildUrl("getRollCall", "id", String.valueOf(rollCallId));
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url,
                ExpirationPolicy.never()
        );
        return response.getRollcall();
    }
    
    @Override
    public LegiscanPeopleView getPerson(int peopleId) {
        String url = buildUrl("getPerson", "id", String.valueOf(peopleId));
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url,
                ExpirationPolicy.weekly()
        );
        
        return response.getPerson();
    }
    
    @Override
    public List<LegiscanDatasetView> getDatasetList(LegiscanState state, Integer year) {
    	String url;
    	
    	if (state != null && year != null) {
    		url = buildUrl("getDatasetList", "state", state.getAbbreviation(), "year", String.valueOf(year));
    	} else if (state != null && year == null) {
    		url = buildUrl("getDatasetList", "state", state.getAbbreviation());
    	} else if (state == null && year != null) {
    		url = buildUrl("getDatasetList", "year", String.valueOf(year));
    	} else {
    		url = buildUrl("getDatasetList");
    	}
    	
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url,
                ExpirationPolicy.weekly()
        );
        
        return response.getDatasetlist();
    }
    
    @Override public LegiscanDatasetView getDataset(int sessionId, String accessKey, String format) { return getDataset(sessionId, accessKey, format, null); }
    public LegiscanDatasetView getDataset(int sessionId, String accessKey, String format, String datasetHash) {
        String url = buildUrl("getDataset", "id", String.valueOf(sessionId), "access_key", accessKey, "format", format);
        String cacheKey = cacheKeyFromUrl(url);

        val metadata = cache.peekEntry(cacheKey);
        val cached = cache.peek(cacheKey, new TypeReference<LegiscanResponse>() {});
    	
    	if (cached.isPresent() && !metadata.get().isExpired()) {
    		LOGGER.trace("Pulling object [" + cacheKey + "] from cache.");
    		return cached.get().getDataset();
    	}
    	
    	// Legiscan requires that we check the change hash against the masterlist here
    	if (cached.isPresent()) {
    		if (datasetHash == null) {
	    		val masterlist = this.getDatasetList(null, null);
	    		datasetHash = masterlist.stream().filter(ds -> Objects.equals(ds.getSessionId(), sessionId)).findFirst().get().getDatasetHash();
    		}
    		
    		// If the latest hash equals the hash of the object we already have, then we know the dataset has not changed and we don't need to download it again.
    		if (cached.get().getDataset().getDatasetHash().equals(datasetHash))
    			return cached.get().getDataset();
    	}
    	
    	LOGGER.debug("Fetching object [" + cacheKey + "] from Legiscan.");
        LegiscanResponse value = makeRequest(url);
        
        val ep = ExpirationPolicy.weekly();
        val ttl = ep.getTtl(Instant.now(), cacheKey);
        cache.put(cacheKey, value, ttl == null ? -1 : ttl.getSeconds());
        
        return value.getDataset();
    }
    
    @Override public byte[] getDatasetRaw(int sessionId, String accessKey, String format) { return getDatasetRaw(sessionId, accessKey, format, null); }
    public byte[] getDatasetRaw(int sessionId, String accessKey, String format, String datasetHash) {
        String url = buildUrl("getDatasetRaw", "id", String.valueOf(sessionId), "access_key", accessKey, "format", format);
        String cacheKey = cacheKeyFromUrl(url);
        
        val metadata = cache.peekEntry(cacheKey);
        val cached = cache.peek(cacheKey, new TypeReference<byte[]>() {});
    	
    	if (cached.isPresent() && !metadata.get().isExpired()) {
    		LOGGER.trace("Pulling object [" + cacheKey + "] from cache.");
    		return cached.get();
    	}
    	
    	// Legiscan requires that we check the change hash against the masterlist here
    	if (cached.isPresent()) {
    		if (datasetHash == null) {
	    		val masterlist = this.getDatasetList(null, null);
	    		datasetHash = masterlist.stream().filter(ds -> Objects.equals(ds.getSessionId(), sessionId)).findFirst().get().getDatasetHash();
    		}
    		
    		// If the latest hash equals the hash of the object we already have, then we know the dataset has not changed and we don't need to download it again.
    		if (Objects.equals(metadata.get().getObjectHash(), datasetHash))
    			return cached.get();
    	}
    	
    	LOGGER.debug("Fetching object [" + cacheKey + "] from Legiscan.");
        byte[] value = makeRequestRaw(url);
        
        val ep = ExpirationPolicy.weekly();
        val expiration = ep.getTtl(Instant.now(), cacheKey);
        cache.put(cacheKey, value, datasetHash, expiration == null ? -1 : expiration.getSeconds());
        
        return value;
    }

    @Override
    public List<LegiscanPeopleView> getSessionPeople(int sessionId) {
        String url = buildUrl("getSessionPeople", "id", String.valueOf(sessionId));
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url,
                ExpirationPolicy.weekly()
        );
        
        return response.getSessionpeople().getPeople();
    }

    @Override
    public List<LegiscanSponsoredBillView> getSponsoredList(int peopleId) {
        String url = buildUrl("getSponsoredList", "id", String.valueOf(peopleId));
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url,
                ExpirationPolicy.daily()
        );
        
        return response.getSponsoredbills();
    }

    @Override
    public List<LegiscanMonitorView> getMonitorList(String record) {
        String url = buildUrl("getMonitorList", "record", record != null ? record : "current");
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url,
                ExpirationPolicy.hourly()
        );
        
        return response.getMonitorlist();
    }

    @Override
    public List<LegiscanMonitorView> getMonitorListRaw(String record) {
        String url = buildUrl("getMonitorListRaw", "record", record != null ? record : "current");
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url,
                ExpirationPolicy.hourly()
        );
        
        return response.getMonitorlist();
    }

}

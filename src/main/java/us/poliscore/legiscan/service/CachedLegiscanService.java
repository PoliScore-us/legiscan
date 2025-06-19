package us.poliscore.legiscan.service;

import java.io.File;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.Getter;
import lombok.SneakyThrows;
import us.poliscore.legiscan.cache.CachedLegiscanDataset;
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
import us.poliscore.legiscan.view.LegiscanSupplementView;

/**
 * Implements a "Legiscan Client", as defined per the Legiscan documentation. Default configuration provides for the following additional services
 * ontop of the standard Legiscan API
 * - File system caching of responses
 * - Bulk populating of datasets
 * - Updating a previously bulk populated dataset and listening to data update events
 */
public class CachedLegiscanService extends LegiscanService {

    private static final Logger LOGGER = Logger.getLogger(CachedLegiscanService.class.getName());

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
    	protected int ttl = 14400; // Default ttl is 4 hours

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
        
        /**
         * @param ttl Sets the time to live (in seconds) for non-static urls. Data will not be fetched more than the specified ttl. Default is 4 hours. Static objects will never be re-fetched.
         * @return
         */
        public Builder withCacheTttl(int ttl) {
        	this.ttl= ttl;
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
                
                // default ttl is 4 hours
                this.cache = new FileSystemLegiscanCache(dir, this.objectMapper, ttl);
            }

            var client = new CachedLegiscanService(apiKey, objectMapper, cache);
            
            return client;
        }
    }
    
    protected LegiscanResponse getOrRequest(String cacheKey, String url) {
    	var cached = cache.getOrExpire(cacheKey).orElse(null);
    	
    	if (cached != null) {
    		LOGGER.fine("Pulling object [" + cacheKey + "] from cache.");
    		return cached;
    	}
    	
    	LOGGER.info("Fetching object [" + cacheKey + "] from Legiscan.");
        LegiscanResponse value = makeRequest(url);
        cache.put(cacheKey, value);
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
    
    public static boolean isCacheKeyStatic(String cacheKey) {
    	var op = cacheKey.split("/")[0].toLowerCase();
    	
    	return ArrayUtils.contains(new String[] { 
    			"getbilltext", "getamendment", "getsupplement", "getrollcall"
    	}, op);
    }
    
    /**
     * Fetches the Legiscan dataset and populates the cache with the most up-to-date data. Any objects which are already cached will simply be updated.
     * The dataset's people, bills and votes can be accessed via the returned CachedLegiscanDataset.
     * 
     * @param dataset
     */
    @SneakyThrows
    public CachedLegiscanDataset cacheDataset(LegiscanDatasetView dataset)
    {
    	var cachedDataset = new CachedLegiscanDataset(this, dataset, objectMapper);
    	
    	cachedDataset.update();
    	
    	return cachedDataset;
    }
    
    /**
     * Fetches the Legiscan dataset and populates the cache with the most up-to-date data. Any objects which are already cached will simply be updated.
     * The dataset's people, bills and votes can be accessed via the returned CachedLegiscanDataset.
     * 
     * @param state
     * @param year
     * @param special
     */
    @SneakyThrows
    public CachedLegiscanDataset cacheDataset(String state, int year, boolean special) {
		List<LegiscanDatasetView> datasets = getDatasetList(state, year);
        
        for (var dataset : datasets)
        {
        	if (dataset.getSpecial() == (special ? 1 : 0)) {
        		return cacheDataset(dataset);
        	}
        }
        
        throw new RuntimeException("Dataset not found!");
	}
    public CachedLegiscanDataset cacheDataset(String state, int year) { return cacheDataset(state,year,false); }

    @Override
    public List<LegiscanSessionView> getSessionList(String state) {
        String url = buildUrl("getSessionList", "state", state);
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url
        );
        return response.getSessions();
    }
    
    @Override
    public LegiscanMasterListView getMasterList(int sessionId) {
        String url = buildUrl("getMasterList", "id", String.valueOf(sessionId));
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url
        );
        return response.getMasterlist();
    }
    
    @Override
    public LegiscanMasterListView getMasterList(String stateCode) {
        String url = buildUrl("getMasterList", "state", stateCode);
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url
        );
        return response.getMasterlist();
    }
    
    @Override
    public LegiscanMasterListView getMasterListRaw(String stateCode) {
        String url = buildUrl("getMasterListRaw", "state", stateCode);
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url
        );
        return response.getMasterlist();
    }
    
    @Override
    public LegiscanMasterListView getMasterListRaw(int sessionId) {
        String url = buildUrl("getMasterListRaw", "id", String.valueOf(sessionId));
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url
        );
        return response.getMasterlist();
    }
    
    public LegiscanBillView getBill(int billId) {
        String url = buildUrl("getBill", "id", String.valueOf(billId));
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url
        );
        
        return response.getBill();
    }
    
    @Override
    public LegiscanBillTextView getBillText(int docId) {
        String url = buildUrl("getBillText", "id", String.valueOf(docId));
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url
        );
        
        return response.getText();
    }
    
    @Override
    public LegiscanAmendmentView getAmendment(int amendmentId) {
        String url = buildUrl("getAmendment", "id", String.valueOf(amendmentId));
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url
        );
        
        return response.getAmendment();
    }
    
    @Override
    public LegiscanSupplementView getSupplement(int supplementId) {
        String url = buildUrl("getSupplement", "id", String.valueOf(supplementId));
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url
        );
        
        return response.getSupplement();
    }
    
    @Override
    public LegiscanRollCallView getRollCall(int rollCallId) {
        String url = buildUrl("getRollCall", "id", String.valueOf(rollCallId));
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url
        );
        return response.getRollcall();
    }
    
    @Override
    public LegiscanPeopleView getPerson(int peopleId) {
        String url = buildUrl("getPerson", "id", String.valueOf(peopleId));
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url
        );
        
        return response.getPerson();
    }
    
    @Override
    public List<LegiscanDatasetView> getDatasetList(String state, Integer year) {
        String url = buildUrl("getDatasetList", "state", state, "year", String.valueOf(year));
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url
        );
        
        return response.getDatasetlist();
    }
    
    @Override
    public LegiscanDatasetView getDataset(int sessionId, String accessKey, String format) {
        String url = buildUrl("getDataset", "id", String.valueOf(sessionId), "access_key", accessKey, "format", format);
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url
        );
        
        return response.getDataset();
    }
    
    @Override
    public byte[] getDatasetRaw(int sessionId, String accessKey, String format) {
        String url = buildUrl("getDatasetRaw", "id", String.valueOf(sessionId), "access_key", accessKey, "format", format);
        String cacheKey = cacheKeyFromUrl(url);
        
        var typeRef = new TypeReference<byte[]>(){};
        
        return cache.getOrExpire(cacheKey, typeRef).orElseGet(() -> {
            byte[] value = makeRequestRaw(url);
            cache.put(cacheKey, value);
            return value;
        });
    }

    @Override
    public List<LegiscanPeopleView> getSessionPeople(int sessionId) {
        String url = buildUrl("getSessionPeople", "id", String.valueOf(sessionId));
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url
        );
        
        return response.getSessionpeople().getPeople();
    }

    @Override
    public List<LegiscanSponsoredBillView> getSponsoredList(int peopleId) {
        String url = buildUrl("getSponsoredList", "id", String.valueOf(peopleId));
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(
                cacheKey,
                url
        );
        
        return response.getSponsoredbills();
    }

    @Override
    public List<LegiscanMonitorView> getMonitorList(String record) {
        String url = buildUrl("getMonitorList", "record", record != null ? record : "current");
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(cacheKey, url);
        return response.getMonitorlist();
    }

    @Override
    public List<LegiscanMonitorView> getMonitorListRaw(String record) {
        String url = buildUrl("getMonitorListRaw", "record", record != null ? record : "current");
        String cacheKey = cacheKeyFromUrl(url);

        LegiscanResponse response = getOrRequest(cacheKey, url);
        return response.getMonitorlist();
    }

}

package us.poliscore.legiscan.cache;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.SneakyThrows;
import net.lingala.zip4j.ZipFile;
import us.poliscore.legiscan.PoliscoreLegiscanUtil;
import us.poliscore.legiscan.service.CachedLegiscanService;
import us.poliscore.legiscan.view.LegiscanBillView;
import us.poliscore.legiscan.view.LegiscanDatasetView;
import us.poliscore.legiscan.view.LegiscanPeopleView;
import us.poliscore.legiscan.view.LegiscanResponse;
import us.poliscore.legiscan.view.LegiscanRollCallView;

public class CachedLegiscanDataset {
	
	private static final Logger LOGGER = Logger.getLogger(CachedLegiscanDataset.class.getName());
	
	@Getter
	protected CachedLegiscanService legiscan;
	
	@Getter
	protected LegiscanDatasetView dataset;
	
	protected ObjectMapper objectMapper;
	
	@Getter
	protected Map<Integer, LegiscanBillView> bills = new HashMap<Integer, LegiscanBillView>();
	
	@Getter
	protected Map<Integer, LegiscanPeopleView> people = new HashMap<Integer, LegiscanPeopleView>();
	
	@Getter
	protected Map<Integer, LegiscanRollCallView> votes = new HashMap<Integer, LegiscanRollCallView>();
	
	public CachedLegiscanDataset(CachedLegiscanService client, LegiscanDatasetView dataset, ObjectMapper objectMapper)
	{
		this.legiscan = client;
		this.dataset = dataset;
		this.objectMapper = objectMapper;
	}
	
	/**
	 * Fetches the Legiscan dataset and populates the cache with the most up-to-date data. Calling this method will populate the bills, people, and votes
	 * member variables. This method is invoked on your behalf when invoking LegiscanClient.cacheDataset.
	 */
	public void update()
	{
		LOGGER.info("Updating dataset [" + dataset.getSessionName() + "] from Legiscan.");
		
		bulkLoad();
		updateBills();
		
		LOGGER.info("Dataset [" + dataset.getSessionName() + "] successfully updated.");
	}
	
	/**
	 * Fetches the dataset via the Legiscan 'bulk loader', by hitting the 'getDatasetRaw' API to receive a zip file, and then loads that zip file
	 * into the legiscan cache. This will load people, bills, and votes.
	 * 
	 * If a bill already exists in the cache it will not be updated; people and votes will be updated. This is because what's in the cache could be
	 * more up-to-date than what we currently have for bills.
	 */
	@SneakyThrows
	protected void bulkLoad()
	{

        byte[] zipBytes = legiscan.getDatasetRaw(dataset.getSessionId(), dataset.getAccessKey(), "json");

        // Write zipBytes to a temporary file
        Path tempZip = Files.createTempFile("dataset-", ".zip");
        
        File file = null;
        
        try
        {
            Files.write(tempZip, zipBytes);

            // Use ZipFile from zip4j to extract
            try (ZipFile zipFile = new ZipFile(tempZip.toFile())) {
                File extractToDir = new File(PoliscoreLegiscanUtil.getDeployedPath(), "cache/" + dataset.getStateId() + "/" + dataset.getYearEnd() + "/" + dataset.getSessionId());
                zipFile.extractAll(extractToDir.getAbsolutePath());
                
                File fPeopleParent = PoliscoreLegiscanUtil.childWithName(extractToDir, "people");
                File fBillParent = PoliscoreLegiscanUtil.childWithName(extractToDir, "bill");
                File fVoteParent = PoliscoreLegiscanUtil.childWithName(extractToDir, "vote");
                
                for(File f : PoliscoreLegiscanUtil.allFilesWhere(fPeopleParent, f -> f.getName().toLowerCase().endsWith(".json")))
                {
                	file = f;
                	var resp = objectMapper.readValue(file, LegiscanResponse.class);
                	var person = resp.getPerson();
                	
                    String cacheKey = LegiscanPeopleView.getCacheKey(person.getPeopleId());
                	
                    legiscan.getCache().put(cacheKey, resp);
                	people.put(person.getPeopleId(), person);
                }
                
                for(File f : PoliscoreLegiscanUtil.allFilesWhere(fBillParent, f -> f.getName().toLowerCase().endsWith(".json")))
                {
                	file = f;
                	
                	var resp = objectMapper.readValue(file, LegiscanResponse.class);
                	var bill = resp.getBill();
                	
                	// This is unfortunate... Legiscan doesn't actually have a 'last update date' concept, they only have a change hash.
                	// For this reason, we cannot replace the bill in the cache if it already exists, because it could be more up-to-date
                	// than what we got from the bulk upload. This should only ever happen with bills, since the refresh frequency for votes
                	// and people is the same for the rest of their API.
                	String cacheKey = LegiscanBillView.getCacheKey(bill.getBillId());
                	var cached = legiscan.getCache().peek(cacheKey).orElse(null);
            		if (cached == null) {
	                    legiscan.getCache().put(cacheKey, resp);
	                	bills.put(bill.getBillId(), bill);
            		} else {
            			bills.put(bill.getBillId(), objectMapper.convertValue(cached.getValue(), new TypeReference<LegiscanResponse>() {}).getBill());
            		}
                }
                
                for(File f : PoliscoreLegiscanUtil.allFilesWhere(fVoteParent, f -> f.getName().toLowerCase().endsWith(".json")))
                {
                	file = f;
                	var resp = objectMapper.readValue(file, LegiscanResponse.class);
                	var rollCall = resp.getRollcall();
                	
                    String cacheKey = LegiscanRollCallView.getCacheKey(rollCall.getRollCallId());
                	
                    legiscan.getCache().put(cacheKey, resp);
                	votes.put(rollCall.getRollCallId(), rollCall);
                }
            }
        }
        catch (Throwable t)
        {
        	Files.deleteIfExists(tempZip);
        	
        	if (file != null)
        		throw new RuntimeException("Encountered problem while processing file [" + file.getAbsolutePath() + "].", t);
        	else
        		throw t;
        }
        
        LOGGER.info("Bulk load complete for dataset [" + dataset.getSessionName() + "] into cache [" + legiscan.getCache().toString() + "]. Dataset contained " + people.size() + " people, " + bills.size()+ " bills, and " + votes.size()+ " votes.");
    
	}
	
	/**
     * Fetches the masterlist and caches all new or updated bills. This is important because the masterlist is updated with new bills every hour but the
     * 'getSessionPeople' or the 'getDataset' APIs are updated weekly. So this makes our bills much more current.
     */
    protected void updateBills()
    {
    	var masterlist = legiscan.getMasterListRaw(dataset.getSessionId());
    	
    	// Count how many
    	long count = 0;
    	for (var summary : masterlist.getBills().values())
    	{
            String cacheKey = LegiscanBillView.getCacheKey(summary.getBillId());
    		
    		var cached = legiscan.getCache().peek(cacheKey).orElse(null);
    		var cachedVal = cached == null ? null : objectMapper.convertValue(cached.getValue(), new TypeReference<LegiscanResponse>() {});
    		
    		if (cached == null || cachedVal.getBill() == null || !summary.getChangeHash().equals(cachedVal.getBill().getChangeHash()))
    		{
    			count++;
    		}
    	}
    	LOGGER.info("Updating bills. Will fetch " + count + " bills from Legiscan.");
    	
    	// Do it
    	for (var summary : masterlist.getBills().values())
    	{
            String cacheKey = LegiscanBillView.getCacheKey(summary.getBillId());
    		
    		var cached = legiscan.getCache().peek(cacheKey).orElse(null);
    		var cachedVal = cached == null ? null : objectMapper.convertValue(cached.getValue(), new TypeReference<LegiscanResponse>() {});
    		
    		if (cached == null || cachedVal.getBill() == null || !summary.getChangeHash().equals(cachedVal.getBill().getChangeHash())) {
    			legiscan.getCache().remove(cacheKey);
    			var bill = legiscan.getBill(summary.getBillId());
    			bills.put(bill.getBillId(), bill);
    		} else if (cached.isExpired()) {
    			// Refresh the TTL here since we just verified with the masterlist that its latest
    			legiscan.getCache().put(cacheKey, cachedVal);
    		}
    	}
    }
}

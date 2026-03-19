package us.poliscore.legiscan.cache;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import net.lingala.zip4j.ZipFile;
import us.poliscore.legiscan.PoliscoreLegiscanUtil;
import us.poliscore.legiscan.service.CachedLegiscanService;
import us.poliscore.legiscan.service.ExpirationPolicy;
import us.poliscore.legiscan.view.LegiscanBillView;
import us.poliscore.legiscan.view.LegiscanDatasetView;
import us.poliscore.legiscan.view.LegiscanPeopleView;
import us.poliscore.legiscan.view.LegiscanResponse;
import us.poliscore.legiscan.view.LegiscanRollCallView;
import us.poliscore.legiscan.view.RefreshFrequency;

public class CachedLegiscanDatasetResult {

	private static final Logger LOGGER = LoggerFactory.getLogger(CachedLegiscanDatasetResult.class);

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

	public CachedLegiscanDatasetResult(CachedLegiscanService client, LegiscanDatasetView dataset,
			ObjectMapper objectMapper) {
		this.legiscan = client;
		this.dataset = dataset;
		this.objectMapper = objectMapper;
	}

	/**
	 * Fetches the Legiscan dataset and populates the cache with the most up-to-date
	 * data. Calling this method will populate the bills, people, and votes member
	 * variables. This method is invoked on your behalf when invoking
	 * LegiscanClient.cacheDataset.
	 * 
	 * @param freq How up-to-date does your data need to be?
	 */
	public void update(RefreshFrequency freq) {
		LOGGER.debug("Updating dataset [" + dataset.getSessionName() + "] from Legiscan.");

		bulkLoad();

		updateBills(freq);

		LOGGER.debug("Dataset [" + dataset.getSessionName() + "] successfully updated.");
	}

	/**
	 * Fetches the dataset via the Legiscan 'bulk loader', by hitting the
	 * 'getDatasetRaw' API to receive a zip file, and then loads that zip file into
	 * the legiscan cache. This will load people, bills, and votes.
	 * 
	 * If a bill already exists in the cache it will not be updated; people and
	 * votes will be updated. This is because what's in the cache could be more
	 * up-to-date than what we currently have for bills.
	 */
	@SneakyThrows
	protected void bulkLoad() {

		byte[] zipBytes = legiscan.getDatasetRaw(dataset.getSessionId(), dataset.getAccessKey(), "json",
				dataset.getDatasetHash());

		// When you fetch people from a bulk zip, it doesn't include bio information for
		// some reason... So we have to fetch people manually.
		val sessionPeople = legiscan.getSessionPeople(dataset.getSessionId());

		// Write zipBytes to a temporary file
		Path tempZip = Files.createTempFile("dataset-", ".zip");

		File file = null;

		try {
			Files.write(tempZip, zipBytes);

			// Use ZipFile from zip4j to extract
			try (ZipFile zipFile = new ZipFile(tempZip.toFile())) {
				File extractToDir = new File(PoliscoreLegiscanUtil.getDeployedPath(),
						"cache/" + dataset.getStateId() + "/" + dataset.getYearEnd() + "/" + dataset.getSessionId());
				zipFile.extractAll(extractToDir.getAbsolutePath());

//                File fPeopleParent = PoliscoreLegiscanUtil.childWithName(extractToDir, "people");
				File fBillParent = PoliscoreLegiscanUtil.childWithName(extractToDir, "bill");
				File fVoteParent = PoliscoreLegiscanUtil.childWithName(extractToDir, "vote");

//                for(File f : PoliscoreLegiscanUtil.allFilesWhere(fPeopleParent, f -> f.getName().toLowerCase().endsWith(".json")))
//                {
//                	file = f;
//                	var resp = objectMapper.readValue(file, LegiscanResponse.class);
//                	var person = resp.getPerson();
//                	
//                    String cacheKey = LegiscanPeopleView.getCacheKey(person.getPeopleId());
//                    val expiration = ExpirationPolicy.weekly().getTtl(Instant.now(), cacheKey);
//                	
//                    legiscan.getCache().put(cacheKey, resp, expiration.getSeconds());
//                	people.put(person.getPeopleId(), person);
//                }

				for (LegiscanPeopleView person : sessionPeople) {
					String cacheKey = LegiscanPeopleView.getCacheKey(person.getPeopleId());
					val expiration = ExpirationPolicy.weekly().getTtl(Instant.now(), cacheKey);

					val resp = new LegiscanResponse();
					resp.setPerson(person);

					legiscan.getCache().put(cacheKey, resp, expiration.getSeconds());
					people.put(person.getPeopleId(), person);
				}

				for (File f : PoliscoreLegiscanUtil.allFilesWhere(fBillParent,
						f -> f.getName().toLowerCase().endsWith(".json"))) {
					file = f;

					var resp = objectMapper.readValue(file, LegiscanResponse.class);
					var bill = resp.getBill();

					// This is unfortunate... Legiscan doesn't actually have a 'last update date'
					// concept, they only have a change hash.
					// For this reason, we cannot replace the bill in the cache if it already
					// exists, because it could be more up-to-date
					// than what we got from the bulk upload. This should only ever happen with
					// bills, since the refresh frequency for votes
					// and people is the same for the rest of their API.
					String cacheKey = LegiscanBillView.getCacheKey(bill.getBillId());
					var cached = legiscan.getCache().peekEntry(cacheKey).orElse(null);
					if (cached == null) {
						val ttl = ExpirationPolicy.fixedDuration(Duration.ofHours(3)).getTtl(Instant.now(), cacheKey);
						legiscan.getCache().put(cacheKey, resp, ttl.getSeconds());
						bills.put(bill.getBillId(), bill);
					} else {
						bills.put(bill.getBillId(),
								objectMapper.convertValue(cached.getValue(), new TypeReference<LegiscanResponse>() {
								}).getBill());
					}
				}

				for (File f : PoliscoreLegiscanUtil.allFilesWhere(fVoteParent,
						f -> f.getName().toLowerCase().endsWith(".json"))) {
					file = f;
					var resp = objectMapper.readValue(file, LegiscanResponse.class);
					var rollCall = resp.getRollcall();

					String cacheKey = LegiscanRollCallView.getCacheKey(rollCall.getRollCallId());

					legiscan.getCache().put(cacheKey, resp, -1);
					votes.put(rollCall.getRollCallId(), rollCall);
				}
			}
		} catch (Throwable t) {
			Files.deleteIfExists(tempZip);

			if (file != null)
				throw new RuntimeException(
						"Encountered problem while processing file [" + file.getAbsolutePath() + "].", t);
			else
				throw t;
		}

		LOGGER.info("Bulk load complete for dataset [" + dataset.getState().getAbbreviation() + "] ["
				+ dataset.getSessionName() + "] into cache [" + legiscan.getCache().toString() + "]. Dataset contained "
				+ people.size() + " people, " + bills.size() + " bills, and " + votes.size() + " votes.");

	}

	/**
	 * Fetches the masterlist and checks to see if the bill we got from bulk loading
	 * matches against the masterlist.
	 *
	 * Behavior by freshness: - WEEKLY: trust the bulk dataset for bills that exist
	 * in bulk, even if the masterlist hash differs. Only fetch manually when the
	 * bill is completely missing from bulk. - More aggressive than WEEKLY (for
	 * example DAILY): manually fetch bills that are missing from bulk or whose
	 * bulk/cached copy does not match the masterlist hash.
	 */
	protected void updateBills(RefreshFrequency freq) {
		var masterlist = legiscan.getMasterListRaw(dataset.getSessionId());

		for (var summary : masterlist.getBills().values()) {
			String cacheKey = LegiscanBillView.getCacheKey(summary.getBillId());

			var cached = legiscan.getCache().peekEntry(cacheKey).orElse(null);
			var cachedVal = cached == null ? null
					: objectMapper.convertValue(cached.getValue(), new TypeReference<LegiscanResponse>() {
					});
			var bulkBill = bills.get(summary.getBillId());

			boolean cachedMatchesMaster = cachedVal != null && cachedVal.getBill() != null
					&& summary.getChangeHash().equals(cachedVal.getBill().getChangeHash());
			boolean bulkMatchesMaster = bulkBill != null && summary.getChangeHash().equals(bulkBill.getChangeHash());
			boolean missingFromBulk = bulkBill == null;
			boolean shouldFetchPerBill = missingFromBulk || (freq != RefreshFrequency.WEEKLY && !bulkMatchesMaster);

			if (!cachedMatchesMaster) {
				legiscan.getCache().remove(cacheKey);

				if (bulkMatchesMaster) {
					// The bulk dataset already has the latest bill, so repair the per-bill cache
					// from bulk data.
					var resp = new LegiscanResponse();
					resp.setBill(bulkBill);
					legiscan.getCache().put(cacheKey, resp, getBillCacheTtlSecs(cacheKey, freq));
				} else if (!missingFromBulk && freq == RefreshFrequency.WEEKLY) {
					// In weekly mode, trust the bulk dataset when the bill exists there, even if
					// the masterlist hash disagrees.
					bulkBill.setChangeHash(summary.getChangeHash());
					var resp = new LegiscanResponse();
					resp.setBill(bulkBill);
					legiscan.getCache().put(cacheKey, resp, getBillCacheTtlSecs(cacheKey, freq));
				} else if (shouldFetchPerBill) {
					var bill = legiscan.getBill(summary.getBillId());
					bills.put(bill.getBillId(), bill);
					long ttlSecs = getBillCacheTtlSecs(cacheKey, freq);

					// This is ultimately a bug on Legiscan's side. If we just fetched the bill fresh, and it still doesn't match the masterlist, then there's something wrong with this picture
    				// We're going to hack around this by simply setting the hash to what it should be and then populating our cache with it so as to avoid spamming legiscan... But something is wrong here.
					if (!bill.getChangeHash().equals(summary.getChangeHash())) {
						LOGGER.error("Legiscan sync error. Bill [" + bill.getBillId() + " : " + bill.getBillNumber()
								+ " " + bill.getBillTypeCode() + "] of dataset " + dataset.getState().getAbbreviation()
								+ " " + dataset.getSessionId()
								+ " was just fetched fresh from legiscan but still didn't match the masterlist change hash. Placing bill on ice for 24 hours to avoid spamming.");
						bill.setChangeHash(summary.getChangeHash());
						ttlSecs = ExpirationPolicy.fixedDuration(Duration.ofHours(24)).getTtl(Instant.now(), cacheKey)
								.getSeconds();
					}

					var resp = new LegiscanResponse();
					resp.setBill(bill);
					legiscan.getCache().put(cacheKey, resp, ttlSecs);
				}
			} else if (cached.isExpired(freq)) {
				// Refresh the TTL here since we just verified with the masterlist that it's
				// latest enough for the requested freshness.
				legiscan.getCache().put(cacheKey, cachedVal, getBillCacheTtlSecs(cacheKey, freq));
			}
		}
	}
	
	protected long getBillCacheTtlSecs(String cacheKey, RefreshFrequency freq) {
		ExpirationPolicy ep = switch (freq) {
		case WEEKLY -> ExpirationPolicy.weekly();
		case DAILY -> ExpirationPolicy.daily();
		default -> ExpirationPolicy.fixedDuration(Duration.ofHours(3));
		};

		return ep.getTtl(Instant.now(), cacheKey).getSeconds();
	}

}

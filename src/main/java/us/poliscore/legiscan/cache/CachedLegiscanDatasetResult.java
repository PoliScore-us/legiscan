package us.poliscore.legiscan.cache;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import us.poliscore.legiscan.view.LegiscanMasterListView;
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

	@Getter
	protected List<Throwable> refreshFailures = new ArrayList<>();

	public CachedLegiscanDatasetResult(CachedLegiscanService client, LegiscanDatasetView dataset,
			ObjectMapper objectMapper) {
		this.legiscan = client;
		this.dataset = dataset;
		this.objectMapper = objectMapper;
	}

	public void addRefreshFailures(List<Throwable> failures) {
		refreshFailures.addAll(failures);
	}

	public boolean hasRefreshFailures() {
		return !refreshFailures.isEmpty();
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

		Map<Integer, LegiscanBillView> bulkBills = new HashMap<Integer, LegiscanBillView>();
		
		bulkLoad(bulkBills);

		updateBillCache(bulkBills, freq);

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
	protected void bulkLoad(Map<Integer, LegiscanBillView> bulkBills) {

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
					
					bulkBills.put(bill.getBillId(), bill);
					bills.put(bill.getBillId(), bill);
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
	 * There are three separate bill objects that have to be reconciled: the bulk bill, the cache bill, and a fresh bill fetched from Legiscan.
	 * 
	 * Legiscan is very clear: the only thing you're allowed to do here is check the bill hash against the master list. If it matches, it's up-to-date. If not, it isn't.
	 * 
	 * The devil here is in the details. The bulk and cached bill could not match the masterlist, but then we could fetch straight from Legiscan and have it still not match the
	 * masterlist.
	 * 
	 * The official Legiscan client is incredibly simplistic and functions as follows:
	 * - bulk load: runs once a week, on schedule, and replaces every bill in the database
	 * - update: runs once a day, loops over the masterlist and fetches bills that don't match the change hash.
	 *
	 * Behavior by freshness: - WEEKLY: prefer the cached or bulk bill when it
	 * agrees with the richer masterlist metadata. - More aggressive than WEEKLY
	 * (for example DAILY): still fall back to getBill when the current copy does not
	 * match the masterlist.
	 */
	protected void updateBillCache(Map<Integer, LegiscanBillView> bulkBills, RefreshFrequency freq) {
		var masterlist = legiscan.getMasterList(dataset.getSessionId());
		var billsToRefresh = new ArrayList<LegiscanMasterListView.BillSummary>();

		for (var summary : masterlist.getBills().values()) {
			String cacheKey = LegiscanBillView.getCacheKey(summary.getBillId());

			var cachedEntry = legiscan.getCache().peekEntry(cacheKey).orElse(null);
			var cachedVal = cachedEntry == null ? null
					: objectMapper.convertValue(cachedEntry.getValue(), new TypeReference<LegiscanResponse>() {
					});
			var cachedBill = cachedVal == null ? null : cachedVal.getBill();
			var bulkBill = bulkBills.get(summary.getBillId());

			boolean cachedMatchesMaster = billMatchesSummary(cachedBill, summary);
			boolean bulkMatchesMaster = billMatchesSummary(bulkBill, summary);

			if (cachedMatchesMaster) {
				bills.put(summary.getBillId(), cachedBill);
				legiscan.getCache().put(cacheKey, cachedVal, getBillCacheTtlSecs(cacheKey, freq));
				continue;
			}

			if (bulkMatchesMaster) {
				var resp = new LegiscanResponse();
				resp.setBill(bulkBill);
				bills.put(summary.getBillId(), bulkBill);
				legiscan.getCache().put(cacheKey, resp, getBillCacheTtlSecs(cacheKey, freq));
				continue;
			}

			if (freq != RefreshFrequency.WEEKLY && (cachedBill == null || cachedEntry == null || cachedEntry.isExpired(null)))
				billsToRefresh.add(summary); // Heads Up : This will cause the bill to be fetched from Legiscan
			else if (cachedBill != null)
				bills.put(summary.getBillId(), cachedBill);
			else if (bulkBill != null)
				bills.put(summary.getBillId(), bulkBill);
		}

		if ((!masterlist.getBills().isEmpty() && billsToRefresh.size() == masterlist.getBills().size())
				|| legiscan.getRequestCount() >= legiscan.getRequestQuotaLimit()) {
			throw new IllegalStateException("Number of required bill refreshes [" + billsToRefresh.size() + "] would exceed quota limit [" + legiscan.getRequestQuotaLimit() + "] for dataset [" + dataset.toString() + "]");
		}
		
		LOGGER.info("About to request " + billsToRefresh.size() + " bills from Legiscan for dataset " + dataset.toString());

		// Request a batch of bills from Legiscan
		for (var summary : billsToRefresh) {
			String cacheKey = LegiscanBillView.getCacheKey(summary.getBillId());
			
			LegiscanBillView bill = legiscan.getBill(summary.getBillId());
			bills.put(bill.getBillId(), bill);

			var resp = new LegiscanResponse();
			resp.setBill(bill);
			legiscan.getCache().put(cacheKey, resp, getBillCacheTtlSecs(cacheKey, freq));
		}
	}
	
//	protected boolean billMatchesSummary(LegiscanBillView bill, LegiscanMasterListView.BillSummary summary) {
//		if (bill == null)
//			return false;
//
//		if (!Objects.equals(summary.getChangeHash(), bill.getChangeHash()))
//			return false;
//
//		if (summary.getStatusDate() != null
//				&& (bill.getStatusDate() == null || bill.getStatusDate().isBefore(summary.getStatusDate())))
//			return false;
//
//		if (summary.getLastActionDate() != null) {
//			LocalDate latestActionDate = latestBillActionDate(bill);
//			if (latestActionDate == null || latestActionDate.isBefore(summary.getLastActionDate()))
//				return false;
//		}
//
//		return true;
//	}
	
	// The only thing we're allowed to do is check the change hash.
	protected boolean billMatchesSummary(LegiscanBillView bill, LegiscanMasterListView.BillSummary summary) {
		if (bill == null || summary == null) return false;
		
		return Objects.equals(summary.getChangeHash(), bill.getChangeHash());
	}

	protected LocalDate latestBillActionDate(LegiscanBillView bill) {
		LocalDate latest = null;

		if (bill.getHistory() != null) {
			for (var history : bill.getHistory()) {
				if (history != null && history.getDate() != null
						&& (latest == null || history.getDate().isAfter(latest))) {
					latest = history.getDate();
				}
			}
		}

		if (bill.getProgress() != null) {
			for (var progress : bill.getProgress()) {
				if (progress != null && progress.getDate() != null
						&& (latest == null || progress.getDate().isAfter(latest))) {
					latest = progress.getDate();
				}
			}
		}

		return latest;
	}

	/**
	 * Compares bills by freshness.
	 *
	 * @return a positive value if candidate is fresher,
	 *         a negative value if current is fresher,
	 *         or zero if they are equally fresh
	 */
	protected int compareBillFreshness(
			LegiscanBillView candidate,
			LegiscanBillView current) {

		if (candidate == current)
			return 0;
		if (candidate == null)
			return -1;
		if (current == null)
			return 1;

		int actionDateComparison = compareNullableDates(
				latestBillActionDate(candidate),
				latestBillActionDate(current));

		if (actionDateComparison != 0)
			return actionDateComparison;

		return compareNullableDates(
				candidate.getStatusDate(),
				current.getStatusDate());
	}

	/**
	 * Treats null as older than any non-null date.
	 */
	private int compareNullableDates(LocalDate candidate, LocalDate current) {
		if (candidate == current)
			return 0;
		if (candidate == null)
			return -1;
		if (current == null)
			return 1;

		return candidate.compareTo(current);
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

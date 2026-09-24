package us.poliscore.legiscan.cache;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import us.poliscore.legiscan.service.CachedLegiscanService;
import us.poliscore.legiscan.view.LegiscanBillView;
import us.poliscore.legiscan.view.LegiscanDatasetView;
import us.poliscore.legiscan.view.LegiscanMasterListView;
import us.poliscore.legiscan.view.LegiscanState;
import us.poliscore.legiscan.view.RefreshFrequency;

class CachedLegiscanDatasetResultTest {

	@Test
	void updateBillsThrowsWhenEveryMasterlistBillWouldNeedRefreshAfterBulkLoad() {
		CachedLegiscanService legiscan = mock(CachedLegiscanService.class);
		LegiscanDatasetView dataset = dataset();
		LegiscanMasterListView masterlist = masterlist(
				summary(101, "new-hash-101"),
				summary(102, "new-hash-102"));

		when(legiscan.getCache()).thenReturn(new NoOpLegiscanCache());
		when(legiscan.getMasterList(dataset.getSessionId())).thenReturn(masterlist);

		CachedLegiscanDatasetResult result = new CachedLegiscanDatasetResult(legiscan, dataset, objectMapper());
		result.bills.put(101, bill(101, "bulk-hash-101"));
		result.bills.put(102, bill(102, "bulk-hash-102"));

		assertThrows(IllegalStateException.class, () -> result.updateBillCache(new HashMap<Integer, LegiscanBillView>(), RefreshFrequency.DAILY));
		verify(legiscan, never()).getBill(101);
		verify(legiscan, never()).getBill(102);
	}

	private ObjectMapper objectMapper() {
		return JsonMapper.builder().addModule(new JavaTimeModule()).build();
	}

	private LegiscanDatasetView dataset() {
		LegiscanDatasetView dataset = new LegiscanDatasetView();
		dataset.setSessionId(1234);
		dataset.setSessionName("2026 Regular Session");
		dataset.setStateId(LegiscanState.COLORADO.getId());
		return dataset;
	}

	private LegiscanMasterListView masterlist(LegiscanMasterListView.BillSummary... summaries) {
		LegiscanMasterListView masterlist = new LegiscanMasterListView();
		for (int i = 0; i < summaries.length; i++) {
			masterlist.getBills().put(String.valueOf(i), summaries[i]);
		}
		return masterlist;
	}

	private LegiscanMasterListView.BillSummary summary(int billId, String changeHash) {
		LegiscanMasterListView.BillSummary summary = new LegiscanMasterListView.BillSummary();
		summary.setBillId(billId);
		summary.setChangeHash(changeHash);
		return summary;
	}

	private LegiscanBillView bill(int billId, String changeHash) {
		LegiscanBillView bill = new LegiscanBillView();
		bill.setBillId(billId);
		bill.setChangeHash(changeHash);
		return bill;
	}
}

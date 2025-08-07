package us.poliscore.legiscan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import us.poliscore.legiscan.view.LegiscanMasterListView;
import us.poliscore.legiscan.view.LegiscanResponse;
import us.poliscore.legiscan.view.LegiscanSessionView;

public class SerializationTest {
	@Test
    public void testSerializeBillsAndSession() throws Exception {
        // Create test data
        LegiscanMasterListView view = new LegiscanMasterListView();

        LegiscanMasterListView.BillSummary bill0 = new LegiscanMasterListView.BillSummary();
        bill0.setBillId(1132030);
        bill0.setNumber("AB1");
        bill0.setChangeHash("d72444d8f2026219e38cb2179dcc67a0");
        bill0.setStatusDate(LocalDate.parse("2024-01-01"));

        LegiscanMasterListView.BillSummary bill1 = new LegiscanMasterListView.BillSummary();
        bill1.setBillId(1131894);
        bill1.setNumber("AB2");
        bill1.setChangeHash("d733e82d03a815e568f92e66f6fd87dc");
        bill1.setStatusDate(LocalDate.parse("2024-01-02"));

        Map<String, LegiscanMasterListView.BillSummary> bills = new HashMap<>();
        bills.put("0", bill0);
        bills.put("1", bill1);
        view.setBills(bills);

        LegiscanSessionView session = new LegiscanSessionView();
        session.setSessionId(123);
        session.setSessionName("2024 Regular Session");
        view.setSession(session);

        // Use ObjectMapper with JavaTimeModule for LocalDate support
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();

        String json = mapper.writeValueAsString(view);

        // Optional: print to visually inspect
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(view));

        // Basic checks
        assertTrue(json.contains("\"0\""));
        assertTrue(json.contains("\"1\""));
        assertTrue(json.contains("\"session\""));
        assertTrue(json.contains("\"bill_id\":1132030"));
        assertTrue(json.contains("\"bill_id\":1131894"));
        assertTrue(json.contains("\"session_name\":\"2024 Regular Session\""));

        // Full structure check (adapt if your LegiscanSessionView has more/less fields)
        String expectedStart = "{\"0\":";
        assertTrue(json.startsWith(expectedStart), "JSON should start with the first bill key");

        // Deserialize
        LegiscanMasterListView result = mapper.readValue(json, LegiscanMasterListView.class);

        // Assertions
        assertNotNull(result);
        assertNotNull(result.getBills());
        assertEquals(2, result.getBills().size());
        assertTrue(result.getBills().containsKey("0"));
        assertTrue(result.getBills().containsKey("1"));
        assertEquals(1132030, result.getBills().get("0").getBillId());
        assertEquals(1131894, result.getBills().get("1").getBillId());

        assertNotNull(result.getSession());
        assertEquals(123, result.getSession().getSessionId());
        assertEquals("2024 Regular Session", result.getSession().getSessionName());
    }
	
	@Test
    public void testDeserializeMasterlist() throws Exception {
        String json = """
        {
          "status":"OK",
          "masterlist":{
            "session":{
              "session_id":2173,
              "state_id":6,
              "year_start":2025,
              "year_end":2025,
              "prefile":0,
              "sine_die":1,
              "prior":0,
              "special":0,
              "session_tag":"Regular Session",
              "session_title":"2025 Regular Session",
              "session_name":"2025 Regular Session"
            },
            "0":{"bill_id":1907917,"number":"HB1001","change_hash":"1e055524011b1ce60b4ce0875c0e1cef"},
            "1":{"bill_id":1907595,"number":"HB1002","change_hash":"4c7c22339f223c51d29770cb9fb4a7b0"},
            "2":{"bill_id":1908011,"number":"HB1003","change_hash":"2f97d54afc7395646757b3e023e29435"},
            "3":{"bill_id":1908008,"number":"HB1004","change_hash":"aea35d280e74631423ed530d9be66599"},
            "4":{"bill_id":1907962,"number":"HB1005","change_hash":"44c79c6901f490e49cf7a7aaf98a307c"},
            "5":{"bill_id":1907852,"number":"HB1006","change_hash":"ba84e7f1c26a93177cb63851b67d5489"},
            "6":{"bill_id":1907875,"number":"HB1007","change_hash":"4db9633e2e7b64c2f9e50428c8a6ad56"},
            "7":{"bill_id":1907976,"number":"HB1008","change_hash":"d661f4ae4621331ae8119d8bf54b742a"},
            "8":{"bill_id":1907710,"number":"HB1009","change_hash":"87a8fd3566d9213b162c3566e85445c3"},
            "9":{"bill_id":1907721,"number":"HB1010","change_hash":"7a964ed9c23603c3c07714e3beeb499b"}
          }
        }
        """;

        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();

        LegiscanResponse response = mapper.readValue(json, LegiscanResponse.class);

        assertNotNull(response);
        assertEquals("OK", response.getStatus());

        LegiscanMasterListView masterlist = response.getMasterlist();
        assertNotNull(masterlist, "Masterlist should not be null");

        // Assert session
        assertNotNull(masterlist.getSession());
        assertEquals(2173, masterlist.getSession().getSessionId());
        assertEquals("2025 Regular Session", masterlist.getSession().getSessionTitle());

        // Assert bills map
        assertNotNull(masterlist.getBills());
        assertEquals(10, masterlist.getBills().size());

        assertTrue(masterlist.getBills().containsKey("0"));
        assertTrue(masterlist.getBills().containsKey("9"));

        assertEquals(1907917, masterlist.getBills().get("0").getBillId());
        assertEquals("HB1001", masterlist.getBills().get("0").getNumber());

        assertEquals(1907721, masterlist.getBills().get("9").getBillId());
        assertEquals("HB1010", masterlist.getBills().get("9").getNumber());

        // Check a random change_hash
        assertEquals("2f97d54afc7395646757b3e023e29435", masterlist.getBills().get("2").getChangeHash());
    }
}

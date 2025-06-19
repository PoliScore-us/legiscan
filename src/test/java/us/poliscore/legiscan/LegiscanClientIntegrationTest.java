package us.poliscore.legiscan;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Because this hits a real Legiscan endpoint, it requires an API key and must be run manually.")
public class LegiscanClientIntegrationTest {

    private static final String TEST_API_KEY = System.getenv("LEGISCAN_API_KEY");

    @Test
    void test_getDatasetList() throws Exception {
        String[] args = {
            "-k", TEST_API_KEY,
            "-op", "getDatasetList", "--state", "CO"
        };
        LegiscanClient.main(args);
    }
    
    @Test
    void test_cacheDataset() throws Exception {
        String[] args = {
            "-k", TEST_API_KEY,
            "-op", "cacheDataset", "--state", "US", "--year", "2024"
        };
        LegiscanClient.main(args);
    }

    @Test
    void test_getDataset() throws Exception {
        String[] args = {
            "-k", TEST_API_KEY,
            "-op", "getDataset", "--id", "2173", "--access_key", "6DYD6JY3DSquQILTTKn0lM"
        };
        LegiscanClient.main(args);
    }

    @Test
    void test_getDatasetRaw() throws Exception {
        String[] args = {
            "-k", TEST_API_KEY,
            "-op", "getDatasetRaw", "--id", "2173", "--access_key", "6DYD6JY3DSquQILTTKn0lM", "--format", "csv"
        };
        LegiscanClient.main(args);
    }

    @Test
    void test_getSessionList() throws Exception {
        String[] args = {
            "-k", TEST_API_KEY,
            "-op", "getSessionList", "--state", "CO"
        };
        LegiscanClient.main(args);
    }

    @Test
    void test_getSessionPeople() throws Exception {
        String[] args = {
            "-k", TEST_API_KEY,
            "-op", "getSessionPeople", "--id", "2173"
        };
        LegiscanClient.main(args);
    }

    @Test
    void test_getMonitorListRaw() throws Exception {
        String[] args = {
            "-k", TEST_API_KEY,
            "-op", "getMonitorListRaw", "--record", "current"
        };
        LegiscanClient.main(args);
    }

    @Test
    void test_getMonitorList() throws Exception {
        String[] args = {
            "-k", TEST_API_KEY,
            "-op", "getMonitorList", "--record", "all"
        };
        LegiscanClient.main(args);
    }

    @Test
    void test_getMasterListRaw() throws Exception {
        String[] args = {
            "-k", TEST_API_KEY,
            "-op", "getMasterListRaw", "--id", "2173"
        };
        LegiscanClient.main(args);
    }

    @Test
    void test_getMasterList() throws Exception {
        String[] args = {
            "-k", TEST_API_KEY,
            "-op", "getMasterList", "--id", "2173"
        };
        LegiscanClient.main(args);
    }

    @Test
    void test_getBill() throws Exception {
        String[] args = {
            "-k", TEST_API_KEY,
            "-op", "getBill", "--id", "2028513"
        };
        LegiscanClient.main(args);
    }

    @Test
    void test_getBillText() throws Exception {
        String[] args = {
            "-k", TEST_API_KEY,
            "-op", "getBillText", "--id", "3240473"
        };
        LegiscanClient.main(args);
    }

    @Test
    void test_getAmendment() throws Exception {
        String[] args = {
            "-k", TEST_API_KEY,
            "-op", "getAmendment", "--id", "255130"
        };
        LegiscanClient.main(args);
    }

    @Test
    void test_getSupplement() throws Exception {
        String[] args = {
            "-k", TEST_API_KEY,
            "-op", "getSupplement", "--id", "607139"
        };
        LegiscanClient.main(args);
    }

    @Test
    void test_getRollCall() throws Exception {
        String[] args = {
            "-k", TEST_API_KEY,
            "-op", "getRollCall", "--id", "1585246"
        };
        LegiscanClient.main(args);
    }

    @Test
    void test_getPerson() throws Exception {
        String[] args = {
            "-k", TEST_API_KEY,
            "-op", "getPerson", "--id", "26447"
        };
        LegiscanClient.main(args);
    }

    @Test
    void test_getSearch() throws Exception {
        String[] args = {
            "-k", TEST_API_KEY,
            "-op", "getSearch", "--state", "CO", "--query", "tax"
        };
        LegiscanClient.main(args);
    }

    @Test
    void test_getSearchRaw() throws Exception {
        String[] args = {
            "-k", TEST_API_KEY,
            "-op", "getSearchRaw", "--state", "CO", "--query", "tax"
        };
        LegiscanClient.main(args);
    }
}

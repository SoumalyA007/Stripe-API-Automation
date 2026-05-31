package dataprovider;

import org.testng.annotations.DataProvider;
import java.util.HashMap;
import java.util.Map;

public class ConnectedAccountsDataProvider {

    /**
     * Provides invalid payloads for creating connected accounts.
     * Each row: testCaseName, accountBody
     */
    @DataProvider(name = "invalidConnectAccountPayloads")
    public static Object[][] invalidConnectAccountPayloads() {
        return new Object[][] {
            {
                "Missing Type",
                createMap(new String[] { "country", "email" }, new Object[] { "US", "test@example.com" })
            },
            {
                "Invalid Type",
                createMap(new String[] { "type", "country" }, new Object[] { "invalid_type", "US" })
            },
            {
                "Invalid Email Format",
                createMap(new String[] { "type", "email" }, new Object[] { "express", "not-an-email" })
            },
            {
                "Invalid Country Code",
                createMap(new String[] { "type", "country" }, new Object[] { "express", "XX" })
            }
        };
    }

    /**
     * Provides invalid account IDs for retrieval, update, and deletion negative tests.
     * Each row: invalidAccountId
     */
    @DataProvider(name = "invalidAccountIds")
    public static Object[][] invalidAccountIds() {
        return new Object[][] {
            { "acct_invalid_12345" },
            { "garbage_not_an_account" },
            { "acct_00000000000000" }
        };
    }

    /**
     * Provides invalid payloads for updating connected accounts.
     * Each row: testCaseName, updateBody
     */
    @DataProvider(name = "invalidUpdatePayloads")
    public static Object[][] invalidUpdatePayloads() {
        return new Object[][] {
            {
                "Invalid Default Currency",
                createMap(new String[] { "default_currency" }, new Object[] { "invalid_currency" })
            }
        };
    }

    // Utility to build a Map from parallel arrays
    private static Map<String, Object> createMap(String[] keys, Object[] values) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }
}

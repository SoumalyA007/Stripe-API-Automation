package dataprovider;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.DataProvider;

import specification.ResponseSpec;

public class ConnectedAccountsDataProvider {

    @DataProvider(name = "invalidConnectAccountPayloads")
    public static Object[][] invalidConnectAccountPayloads() {
        return new Object[][] {
                // {
                //         "Missing Type",
                //         createMap(new String[] { "country", "email" }, new Object[] { "US", "test@example.com" })
                // },
                {
                        "Invalid Type",
                        createMap(new String[] { "type", "country" }, new Object[] { "invalid_type", "US","type" }),"type"
                },
                {
                        "Invalid Email Format",
                        createMap(new String[] { "type", "email" }, new Object[] { "express", "not-an-email","email" }),"email"
                },
                {
                        "Invalid Country Code",
                        createMap(new String[] { "type", "country" }, new Object[] { "express", "XX","country" }),"country"
                }
        };
    }

    @DataProvider(name = "invalidAccountIds")
    public static Object[][] invalidAccountIds() {
        return new Object[][] {
                { "acct_invalid_12345",ResponseSpec.forbidden() },
                { "garbage_not_an_account",ResponseSpec.forbidden() },
                { "acct_00000000000000",ResponseSpec.forbidden() }
        };
    }

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

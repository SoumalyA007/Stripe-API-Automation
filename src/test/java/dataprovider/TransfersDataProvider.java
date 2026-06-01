package dataprovider;

import org.testng.annotations.DataProvider;
import java.util.HashMap;
import java.util.Map;

public class TransfersDataProvider {

    @DataProvider(name = "invalidTransferPayloads")
    public Object[][] invalidTransferPayloads() {
        return new Object[][] {
                { "Missing Destination", createMap(new String[] { "amount", "currency" }, new Object[] { 1000, "usd" }) },
                { "Missing Amount", createMap(new String[] { "currency", "destination" }, new Object[] { "usd", "acct_123" }) },
                { "Missing Currency", createMap(new String[] { "amount", "destination" }, new Object[] { 1000, "acct_123" }) },
        };
    }

    @DataProvider(name = "invalidTransferIds")
    public Object[][] invalidTransferIds() {
        return new Object[][] {
                { "Invalid prefix ID", "tr_invalid_id_12345", "No such transfer" },
                { "Garbage ID", "garbage_not_a_transfer", "No such transfer" },
                { "Truncated prefix", "tr_", "No such transfer" }
        };
    }

    private static Map<String, Object> createMap(String[] keys, Object[] values) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }
}

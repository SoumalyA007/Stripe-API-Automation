package dataprovider;

import org.testng.annotations.DataProvider;
import java.util.HashMap;
import java.util.Map;

public class PayoutsDataProvider {

    @DataProvider(name = "invalidPayoutPayloads")
    public Object[][] invalidPayoutPayloads() {
        return new Object[][] {
                { "Missing Amount", createMap(new String[] { "currency" }, new Object[] { "usd" }) },
                { "Missing Currency", createMap(new String[] { "amount" }, new Object[] { 1000 }) }
        };
    }

    @DataProvider(name = "invalidPayoutIds")
    public Object[][] invalidPayoutIds() {
        return new Object[][] {
                { "Invalid prefix ID", "po_invalid_id_12345", "No such payout" },
                { "Garbage ID", "garbage_not_a_payout", "No such payout" },
                { "Truncated prefix", "po_", "No such payout" }
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

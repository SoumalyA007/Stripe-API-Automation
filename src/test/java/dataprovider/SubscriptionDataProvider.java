package dataprovider;

import org.testng.annotations.DataProvider;

import java.util.HashMap;
import java.util.Map;

public class SubscriptionDataProvider {

        @DataProvider(name = "subscriptionIntervals")
        public static Object[][] subscriptionIntervals() {
                return new Object[][] {
                                { "Monthly Plan", 1500, "usd", "month" },
                                { "Yearly Plan", 15000, "usd", "year" },
                                { "Weekly Plan", 500, "usd", "week" },
                };
        }

        @DataProvider(name = "subscriptionMetadataUpdates")
        public static Object[][] subscriptionMetadataUpdates() {
                return new Object[][] {
                                { "plan_type", "premium" },
                                { "referral_code", "REF_" + System.currentTimeMillis() },
                                { "environment", "qa_automation" },
                };
        }

        @DataProvider(name = "invalidSubscriptionIds")
        public static Object[][] invalidSubscriptionIds() {
                return new Object[][] {
                                { "sub_invalid_id_12345" },
                                { "***" },
                                { "sub_00000000000000" },
                };
        }

        @DataProvider(name = "invalidProductBodies")
        public static Object[][] invalidProductBodies() {
                return new Object[][] {
                                {
                                                "Missing Name",
                                                createMap(new String[] { "type" }, new Object[] { "service" })
                                },
                                {
                                                "Empty Name",
                                                createMap(new String[] { "name", "type" },
                                                                new Object[] { "", "service" })
                                },
                };
        }

        @DataProvider(name = "invalidPriceBodies")
        public static Object[][] invalidPriceBodies() {
                return new Object[][] {
                                {
                                                "Missing Product",
                                                createMap(
                                                                new String[] { "unit_amount", "currency",
                                                                                "recurring[interval]" },
                                                                new Object[] { 1000, "usd", "month" })
                                },
                                {
                                                "Negative Amount",
                                                createMap(
                                                                new String[] { "product", "unit_amount", "currency",
                                                                                "recurring[interval]" },
                                                                new Object[] { "prod_placeholder", -100, "usd",
                                                                                "month" })
                                },
                                {
                                                "Invalid Currency",
                                                createMap(
                                                                new String[] { "product", "unit_amount", "currency",
                                                                                "recurring[interval]" },
                                                                new Object[] { "prod_placeholder", 1000, "zzz",
                                                                                "month" })
                                },
                };
        }

        // Utility to quickly build a Map from parallel arrays
        private static Map<String, Object> createMap(String[] keys, Object[] values) {
                Map<String, Object> map = new HashMap<>();
                for (int i = 0; i < keys.length; i++) {
                        map.put(keys[i], values[i]);
                }
                return map;
        }
}

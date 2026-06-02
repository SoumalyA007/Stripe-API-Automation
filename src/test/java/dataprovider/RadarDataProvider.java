package dataprovider;

import org.testng.annotations.DataProvider;

public class RadarDataProvider {

    @DataProvider(name = "invalidWarningIds")
    public Object[][] invalidWarningIds() {
        return new Object[][] {
                { "Invalid prefix ID", "issfw_invalid_id_12345", "No such early fraud warning" },
                { "Garbage ID", "garbage_not_a_warning", "No such early fraud warning" },
                { "Truncated prefix", "issfw_", "No such early fraud warning" }
        };
    }
}

package dataprovider;

import org.testng.annotations.DataProvider;

public class RadarDataProvider {

    @DataProvider(name = "invalidWarningIds")
    public Object[][] invalidWarningIds() {
        return new Object[][] {
                { "Invalid prefix ID", "issfw_invalid_id_12345", "No such radar.early_fraud_warning:" },
                { "Garbage ID", "garbage_not_a_warning", "No such radar.early_fraud_warning:" },
                { "Truncated prefix", "issfw_", "No such radar.early_fraud_warning:" }
        };
    }
}

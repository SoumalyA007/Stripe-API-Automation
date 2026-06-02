package dataprovider;

import org.testng.annotations.DataProvider;

public class DisputesDataProvider {

    @DataProvider(name = "invalidDisputeIds")
    public Object[][] invalidDisputeIds() {
        return new Object[][] {
                { "Invalid prefix ID", "dp_invalid_id_12345", "No such dispute" },
                { "Garbage ID", "garbage_not_a_dispute", "No such dispute" },
                { "Truncated prefix", "dp_", "No such dispute" }
        };
    }
}

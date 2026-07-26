package dataprovider;

import org.testng.annotations.DataProvider;

public class RefundDataProvider {

    // ============== NEGATIVE: Invalid Refund Reasons ==============

    @DataProvider(name = "invalidRefundReasons")
    public Object[][] invalidRefundReasons() {
        return new Object[][] {
                { "Random invalid string", "not_a_valid_reason", "reason" },
                { "Numeric value", "99999", "reason" },
                { "Wrong casing on valid", "Requested_By_Customer", "reason" },
                { "Special characters", "!@#$%^", "reason" }
        };
    }

    // ============== NEGATIVE: Invalid Refund IDs (Retrieve & Cancel)
    // ==============

    @DataProvider(name = "invalidRefundIds")
    public Object[][] invalidRefundIds() {
        return new Object[][] {
                { "Invalid prefix ID", "re_invalid_id_12345", "No such refund" },
                { "Garbage ID", "garbage_not_a_refund", "No such refund" },
                { "Truncated prefix", "re_", "No such refund" }
        };
    }

}

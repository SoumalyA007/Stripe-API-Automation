package dataprovider;

import org.testng.annotations.DataProvider;
import specification.ResponseSpec;

public class SetupIntentDataProvider {

    @DataProvider(name = "declinedCardTokensForSetupIntent")
    public static Object[][] declinedCardTokensForSetupIntent() {
        return new Object[][] {
                { "Declined - Generic", "tok_chargeDeclined", "card_declined" },
                { "Declined - Insufficient Funds", "tok_visa_chargeDeclinedInsufficientFunds", "card_declined" },
                { "Declined - Expired Card", "tok_chargeDeclinedExpiredCard", "expired_card" },
                { "Declined - Processing Error", "tok_chargeDeclinedProcessingError", "processing_error" },
        };
    }

    @DataProvider(name = "setupIntentMetadataUpdates")
    public static Object[][] setupIntentMetadataUpdates() {
        return new Object[][] {
                { "order_id", "ord_" + System.currentTimeMillis() },
                { "source", "automation_test" },
                { "environment", "staging" },
        };
    }

    @DataProvider(name = "invalidSetupIntentIds")
    public static Object[][] invalidSetupIntentIds() {
        return new Object[][] {
                { "seti_invalid_id_12345", ResponseSpec.not_found() },
                { "***", ResponseSpec.not_found() },
                { "", ResponseSpec.not_found() },
        };
    }

    @DataProvider(name = "cancellationReasons")
    public static Object[][] cancellationReasons() {
        return new Object[][] {
                { "abandoned" },
                { "requested_by_customer" },
                { "duplicate" },
        };
    }
}

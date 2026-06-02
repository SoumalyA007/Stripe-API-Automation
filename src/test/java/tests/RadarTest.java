package tests;

import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import dataprovider.RadarDataProvider;
import endpoints.Radar;
import helpers.RadarHelper;
import helpers.TestContext;
import org.testng.annotations.Test;
import specification.ResponseSpec;
import testbase.BaseClass;

public class RadarTest extends BaseClass {

    // ***************RETRIEVE EARLY FRAUD WARNING – POSITIVE*******************\\

    @Test(groups = { "unit" })
    public void TC_01_Retrieve_EarlyFraudWarning() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

        String warningId = TestContext.getEarlyFraudWarningId();
        if (warningId == null) {
            warningId = RadarHelper.createFallbackEarlyFraudWarning();
            logger.info("Created fallback early fraud warning ID: {}", warningId);
        }

        Radar.retrieveEarlyFraudWarning(warningId)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(warningId))
                .body("object", equalTo("radar.early_fraud_warning"));
    }

    // ***************LIST EARLY FRAUD WARNINGS – POSITIVE*******************\\

    @Test(groups = { "unit" })
    public void TC_02_List_EarlyFraudWarnings() {
        Map<String, Object> query = new HashMap<>();
        query.put("limit", 3);

        Radar.listEarlyFraudWarnings(query)
                .then()
                .spec(ResponseSpec.OK())
                .body("object", equalTo("list"))
                .body("data", notNullValue());
    }

    // ***************RETRIEVE EARLY FRAUD WARNING – NEGATIVE*******************\\

    @Test(groups = { "unit" }, dataProvider = "invalidWarningIds", dataProviderClass = RadarDataProvider.class)
    public void TC_03_RetrieveEarlyFraudWarning_InvalidId(String testCaseName, String warningId, String expectedErrorFragment) {
        logger.info("Running retrieve early fraud warning invalid case: {}", testCaseName);

        Radar.retrieveEarlyFraudWarning(warningId)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.message", containsString(expectedErrorFragment));
    }

    @Test(groups = { "unit" })
    public void TC_04_RetrieveEarlyFraudWarning_InvalidAuth() {
        Radar.retrieveEarlyFraudWarningWithCustomAuth("sk_test_invalid_key_12345", "issfw_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));
    }

    @Test(groups = { "unit" })
    public void TC_05_RetrieveEarlyFraudWarning_MissingAuth() {
        Radar.retrieveEarlyFraudWarningWithCustomAuth(null, "issfw_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("You did not provide an API key"));
    }
}

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

    @Test(groups = { "radar", "positive", "smoke", "regression" })
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

        logger.info("Successfully retrieved early fraud warning ID: {}", warningId);
    }

    // ***************LIST EARLY FRAUD WARNINGS – POSITIVE*******************\\

    @Test(groups = { "radar", "positive", "regression" })
    public void TC_02_List_EarlyFraudWarnings() {
        logger.info("Listing early fraud warnings with limit 3");
        Map<String, Object> query = new HashMap<>();
        query.put("limit", 3);

        Radar.listEarlyFraudWarnings(query)
                .then()
                .spec(ResponseSpec.OK())
                .body("object", equalTo("list"))
                .body("data", notNullValue());

        logger.info("Successfully listed early fraud warnings");
    }

    // ***************RETRIEVE EARLY FRAUD WARNING – NEGATIVE*******************\\

    @Test(groups = { "radar", "negative", "regression" }, dataProvider = "invalidWarningIds", dataProviderClass = RadarDataProvider.class)
    public void TC_03_RetrieveEarlyFraudWarning_InvalidId(String testCaseName, String warningId,
            String expectedErrorFragment) {
        logger.info("Running retrieve early fraud warning invalid case: {} for warning ID: {}", testCaseName,
                warningId);

        Radar.retrieveEarlyFraudWarning(warningId)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.message", containsString(expectedErrorFragment));

        logger.info("Successfully verified not_found response with message containing '{}'", expectedErrorFragment);
    }

    @Test(groups = { "radar", "negative", "auth", "regression" })
    public void TC_04_RetrieveEarlyFraudWarning_InvalidAuth() {
        logger.info("Testing retrieve early fraud warning with invalid auth key");
        Radar.retrieveEarlyFraudWarningWithCustomAuth("sk_test_invalid_key_12345", "issfw_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));

        logger.info("Successfully verified unauthorized response for invalid auth");
    }

    @Test(groups = { "radar", "negative", "auth", "regression" })
    public void TC_05_RetrieveEarlyFraudWarning_MissingAuth() {
        logger.info("Testing retrieve early fraud warning with missing auth key");
        Radar.retrieveEarlyFraudWarningWithCustomAuth(null, "issfw_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("You did not provide an API key"));

        logger.info("Successfully verified unauthorized response for missing auth");
    }
}

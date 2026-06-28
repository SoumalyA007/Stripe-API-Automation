package tests;

import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataprovider.RadarDataProvider;
import endpoints.Radar;
import helpers.RadarHelper;
import helpers.PojoValidator;
import helpers.TestContext;
import models.response.RadarEarlyFraudWarningResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import specification.ResponseSpec;
import testbase.BaseClass;

public class RadarTest extends BaseClass {

    List<String> fallbackEarlyFraudWarningIds = new ArrayList<>();

    // ***************RETRIEVE EARLY FRAUD WARNING – POSITIVE*******************\\

    @Test(groups = { "radar", "regression", "retrieve_list_fraud" })
    public void TC_01_Retrieve_EarlyFraudWarning() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

        String warningId = TestContext.getEarlyFraudWarningId();
        if (warningId == null) {
            warningId = RadarHelper.createFallbackEarlyFraudWarning();
            fallbackEarlyFraudWarningIds.add(warningId);
            logger.info("Created fallback early fraud warning ID --> {}", warningId);
        } else {
            logger.info("Fetched early fraud warning ID from context --> {}", warningId);
        }

        io.restassured.response.Response resp = Radar.retrieveEarlyFraudWarning(warningId);
        resp.then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(warningId))
                .body("object", equalTo("radar.early_fraud_warning"));

        RadarEarlyFraudWarningResponse efwResponse = resp.as(RadarEarlyFraudWarningResponse.class);
        PojoValidator.validate(efwResponse);
        logger.info("POJO validation passed for radar early fraud warning: {}", warningId);
        logger.info("Successfully retrieved early fraud warning ID: {}", warningId);
    }

    // ***************LIST EARLY FRAUD WARNINGS – POSITIVE*******************\\

    @Test(groups = { "radar", "regression", "retrieve_list_fraud" })
    public void TC_02_List_EarlyFraudWarnings() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));
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

    @Test(groups = { "radar", "negative",
            "regression" }, dataProvider = "invalidWarningIds", dataProviderClass = RadarDataProvider.class)
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
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));
        Radar.retrieveEarlyFraudWarningWithCustomAuth("sk_test_invalid_key_12345", "issfw_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));

        logger.info("Successfully verified unauthorized response for invalid auth");
    }

    @Test(groups = { "radar", "negative", "auth", "regression" })
    public void TC_05_RetrieveEarlyFraudWarning_MissingAuth() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));
        Radar.retrieveEarlyFraudWarningWithCustomAuth(null, "issfw_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("You did not provide an API key"));

        logger.info("Successfully verified unauthorized response for missing auth");
    }

    // ***************CLEANUP*******************\\

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        logger.info("🧹 Starting cleanup for RadarTest...");

        // Early fraud warnings cannot be deleted via the API — log them for reference
        if (!fallbackEarlyFraudWarningIds.isEmpty()) {
            logger.info(
                    "ℹ️ {} fallback early fraud warning(s) were created during the test run (cannot be deleted via API):",
                    fallbackEarlyFraudWarningIds.size());
            for (String id : fallbackEarlyFraudWarningIds) {
                logger.info("   - Early Fraud Warning ID: {}", id);
            }
        }

        // Clear shared ID from TestContext to avoid state leakage
        TestContext.setEarlyFraudWarningId(null);
        logger.info("🧹 Cleared early fraud warning ID from TestContext.");

        logger.info("✅ Cleanup complete for RadarTest.");
    }
}

package tests;

import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import dataprovider.DisputesDataProvider;
import endpoints.Disputes;
import helpers.DisputesHelper;
import helpers.TestContext;
import org.testng.annotations.Test;
import specification.ResponseSpec;
import testbase.BaseClass;

public class DisputesTest extends BaseClass {

    // ***************RETRIEVE DISPUTE – POSITIVE*******************\\

    @Test(groups = { "dispute", "regression" })
    public void TC_01_Retrieve_Dispute() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

        String disputeId = TestContext.getDisputeId();
        if (disputeId == null) {
            disputeId = DisputesHelper.createFallbackDispute();
            logger.info("Created fallback dispute ID: {}", disputeId);
        }

        Disputes.retrieveDispute(disputeId)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(disputeId))
                .body("object", equalTo("dispute"));

        logger.info("Successfully retrieved dispute ID: {}", disputeId);
    }

    // ***************UPDATE DISPUTE EVIDENCE – POSITIVE*******************\\

    @Test(groups = { "dispute", "regression" })
    public void TC_02_Update_Dispute_Evidence() {
        String disputeId = TestContext.getDisputeId();
        if (disputeId == null) {
            disputeId = DisputesHelper.createFallbackDispute();
            logger.info("Created fallback dispute ID: {}", disputeId);
        }

        Map<String, Object> evidence = new HashMap<>();
        evidence.put("customer_name", "Jane Doe");
        evidence.put("product_description", "Premium Subscription Services");

        Map<String, Object> body = new HashMap<>();
        body.put("evidence", evidence);

        logger.info("Updating dispute evidence for ID: {}", disputeId);
        Disputes.updateDispute(disputeId, body)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(disputeId))
                .body("evidence.customer_name", equalTo("Jane Doe"))
                .body("evidence.product_description", equalTo("Premium Subscription Services"));

        logger.info("Successfully updated dispute evidence for ID: {}", disputeId);
    }

    // ***************CLOSE DISPUTE – POSITIVE*******************\\

    @Test(groups = { "dispute", "regression" })
    public void TC_03_Close_Dispute() {
        String disputeId = TestContext.getDisputeId();
        if (disputeId == null) {
            disputeId = DisputesHelper.createFallbackDispute();
            logger.info("Created fallback dispute ID: {}", disputeId);
        }

        logger.info("Closing dispute with ID: {}", disputeId);
        Disputes.closeDispute(disputeId)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(disputeId))
                .body("status", equalTo("lost")); // Closing a dispute in test mode sets status to 'lost'

        logger.info("Successfully closed dispute with ID: {}", disputeId);
    }

    // ***************LIST DISPUTES – POSITIVE*******************\\

    @Test(groups = { "dispute", "regression" })
    public void TC_04_List_Disputes() {
        logger.info("Listing disputes with limit 3");
        Map<String, Object> query = new HashMap<>();
        query.put("limit", 3);

        Disputes.listDisputes(query)
                .then()
                .spec(ResponseSpec.OK())
                .body("object", equalTo("list"))
                .body("data", notNullValue());

        logger.info("Successfully listed disputes");
    }

    // ***************RETRIEVE DISPUTE – NEGATIVE*******************\\

    @Test(groups = { "dispute", "negative", "regression" }, dataProvider = "invalidDisputeIds", dataProviderClass = DisputesDataProvider.class)
    public void TC_05_RetrieveDispute_InvalidId(String testCaseName, String disputeId, String expectedErrorFragment) {
        logger.info("Running retrieve dispute invalid case: {} for dispute ID: {}", testCaseName, disputeId);

        Disputes.retrieveDispute(disputeId)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.message", containsString(expectedErrorFragment));

        logger.info("Successfully verified retrieve dispute invalid ID failure: {}", expectedErrorFragment);
    }

    @Test(groups = { "dispute", "negative", "auth", "regression" })
    public void TC_06_RetrieveDispute_InvalidAuth() {
        logger.info("Testing retrieve dispute with invalid auth key");
        Disputes.retrieveDisputeWithCustomAuth("sk_test_invalid_key_12345", "dp_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));

        logger.info("Successfully verified unauthorized response for retrieve dispute invalid auth");
    }

    @Test(groups = { "dispute", "negative", "auth", "regression" })
    public void TC_07_RetrieveDispute_MissingAuth() {
        logger.info("Testing retrieve dispute with missing auth key");
        Disputes.retrieveDisputeWithCustomAuth(null, "dp_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("You did not provide an API key"));

        logger.info("Successfully verified unauthorized response for retrieve dispute missing auth");
    }

    // ***************UPDATE DISPUTE – NEGATIVE*******************\\

    @Test(groups = { "dispute", "negative", "regression" })
    public void TC_08_UpdateDispute_InvalidId() {
        logger.info("Testing update dispute with invalid ID");
        Map<String, Object> body = new HashMap<>();
        body.put("evidence", Map.of("customer_name", "Jane"));

        Disputes.updateDispute("dp_invalid_id_12345", body)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.message", containsString("No such dispute"));

        logger.info("Successfully verified update dispute invalid ID failure");
    }

    @Test(groups = { "dispute", "negative", "auth", "regression" })
    public void TC_09_UpdateDispute_InvalidAuth() {
        logger.info("Testing update dispute with invalid auth");
        Map<String, Object> body = new HashMap<>();
        body.put("evidence", Map.of("customer_name", "Jane"));

        Disputes.updateDisputeWithCustomAuth("sk_test_invalid_key_12345", "dp_any_id", body)
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));

        logger.info("Successfully verified unauthorized response for update dispute invalid auth");
    }

    @Test(groups = { "dispute", "negative", "auth", "regression" })
    public void TC_10_UpdateDispute_MissingAuth() {
        logger.info("Testing update dispute with missing auth");
        Map<String, Object> body = new HashMap<>();
        body.put("evidence", Map.of("customer_name", "Jane"));

        Disputes.updateDisputeWithCustomAuth(null, "dp_any_id", body)
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("You did not provide an API key"));

        logger.info("Successfully verified unauthorized response for update dispute missing auth");
    }

    // ***************CLOSE DISPUTE – NEGATIVE*******************\\

    @Test(groups = { "dispute", "negative", "regression" })
    public void TC_11_CloseDispute_InvalidId() {
        logger.info("Testing close dispute with invalid ID");
        Disputes.closeDispute("dp_invalid_id_12345")
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.message", containsString("No such dispute"));

        logger.info("Successfully verified close dispute invalid ID failure");
    }

    @Test(groups = { "dispute", "negative", "auth", "regression" })
    public void TC_12_CloseDispute_InvalidAuth() {
        logger.info("Testing close dispute with invalid auth");
        Disputes.closeDisputeWithCustomAuth("sk_test_invalid_key_12345", "dp_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));

        logger.info("Successfully verified unauthorized response for close dispute invalid auth");
    }

    @Test(groups = { "dispute", "negative", "auth", "regression" })
    public void TC_13_CloseDispute_MissingAuth() {
        logger.info("Testing close dispute with missing auth");
        Disputes.closeDisputeWithCustomAuth(null, "dp_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("You did not provide an API key"));

        logger.info("Successfully verified unauthorized response for close dispute missing auth");
    }

    @Test(groups = { "dispute", "regression" })
    public void TC_14_positive_Idempotent_UpdateDispute_Evidence() {
        String disputeId = TestContext.getDisputeId();
        if (disputeId == null) {
            disputeId = DisputesHelper.createFallbackDispute();
            logger.info("Created fallback dispute ID: {}", disputeId);
        }

        Map<String, Object> evidence = new HashMap<>();
        evidence.put("customer_name", "John Doe");

        Map<String, Object> body = new HashMap<>();
        body.put("evidence", evidence);

        Map<String, String> headers = new HashMap<>();
        String idempotencyKey = "disp_key_" + System.currentTimeMillis();
        headers.put("Idempotency-Key", idempotencyKey);

        logger.info("Testing idempotent update dispute evidence with key: {}", idempotencyKey);
        io.restassured.response.Response firstResponse = Disputes.updateDispute(disputeId, body, headers)
                .then()
                .spec(ResponseSpec.OK())
                .body("evidence.customer_name", equalTo("John Doe"))
                .extract()
                .response();

        String firstEvidenceName = firstResponse.jsonPath().getString("evidence.customer_name");

        io.restassured.response.Response secondResponse = Disputes.updateDispute(disputeId, body, headers)
                .then()
                .spec(ResponseSpec.OK())
                .body("evidence.customer_name", equalTo("John Doe"))
                .extract()
                .response();

        String secondEvidenceName = secondResponse.jsonPath().getString("evidence.customer_name");

        org.testng.Assert.assertEquals(firstEvidenceName, secondEvidenceName);
        logger.info("Successfully verified idempotent update dispute evidence for key: {}", idempotencyKey);
    }
}

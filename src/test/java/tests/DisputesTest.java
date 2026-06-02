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

    @Test(groups = { "unit" })
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
    }

    // ***************UPDATE DISPUTE EVIDENCE – POSITIVE*******************\\

    @Test(groups = { "unit" })
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

        Disputes.updateDispute(disputeId, body)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(disputeId))
                .body("evidence.customer_name", equalTo("Jane Doe"))
                .body("evidence.product_description", equalTo("Premium Subscription Services"));
    }

    // ***************CLOSE DISPUTE – POSITIVE*******************\\

    @Test(groups = { "unit" })
    public void TC_03_Close_Dispute() {
        String disputeId = TestContext.getDisputeId();
        if (disputeId == null) {
            disputeId = DisputesHelper.createFallbackDispute();
            logger.info("Created fallback dispute ID: {}", disputeId);
        }

        Disputes.closeDispute(disputeId)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(disputeId))
                .body("status", equalTo("lost")); // Closing a dispute in test mode sets status to 'lost'
    }

    // ***************LIST DISPUTES – POSITIVE*******************\\

    @Test(groups = { "unit" })
    public void TC_04_List_Disputes() {
        Map<String, Object> query = new HashMap<>();
        query.put("limit", 3);

        Disputes.listDisputes(query)
                .then()
                .spec(ResponseSpec.OK())
                .body("object", equalTo("list"))
                .body("data", notNullValue());
    }

    // ***************RETRIEVE DISPUTE – NEGATIVE*******************\\

    @Test(groups = { "unit" }, dataProvider = "invalidDisputeIds", dataProviderClass = DisputesDataProvider.class)
    public void TC_05_RetrieveDispute_InvalidId(String testCaseName, String disputeId, String expectedErrorFragment) {
        logger.info("Running retrieve dispute invalid case: {}", testCaseName);

        Disputes.retrieveDispute(disputeId)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.message", containsString(expectedErrorFragment));
    }

    @Test(groups = { "unit" })
    public void TC_06_RetrieveDispute_InvalidAuth() {
        Disputes.retrieveDisputeWithCustomAuth("sk_test_invalid_key_12345", "dp_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));
    }

    @Test(groups = { "unit" })
    public void TC_07_RetrieveDispute_MissingAuth() {
        Disputes.retrieveDisputeWithCustomAuth(null, "dp_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("You did not provide an API key"));
    }

    // ***************UPDATE DISPUTE – NEGATIVE*******************\\

    @Test(groups = { "unit" })
    public void TC_08_UpdateDispute_InvalidId() {
        Map<String, Object> body = new HashMap<>();
        body.put("evidence", Map.of("customer_name", "Jane"));

        Disputes.updateDispute("dp_invalid_id_12345", body)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.message", containsString("No such dispute"));
    }

    @Test(groups = { "unit" })
    public void TC_09_UpdateDispute_InvalidAuth() {
        Map<String, Object> body = new HashMap<>();
        body.put("evidence", Map.of("customer_name", "Jane"));

        Disputes.updateDisputeWithCustomAuth("sk_test_invalid_key_12345", "dp_any_id", body)
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));
    }

    @Test(groups = { "unit" })
    public void TC_10_UpdateDispute_MissingAuth() {
        Map<String, Object> body = new HashMap<>();
        body.put("evidence", Map.of("customer_name", "Jane"));

        Disputes.updateDisputeWithCustomAuth(null, "dp_any_id", body)
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("You did not provide an API key"));
    }

    // ***************CLOSE DISPUTE – NEGATIVE*******************\\

    @Test(groups = { "unit" })
    public void TC_11_CloseDispute_InvalidId() {
        Disputes.closeDispute("dp_invalid_id_12345")
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.message", containsString("No such dispute"));
    }

    @Test(groups = { "unit" })
    public void TC_12_CloseDispute_InvalidAuth() {
        Disputes.closeDisputeWithCustomAuth("sk_test_invalid_key_12345", "dp_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));
    }

    @Test(groups = { "unit" })
    public void TC_13_CloseDispute_MissingAuth() {
        Disputes.closeDisputeWithCustomAuth(null, "dp_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("You did not provide an API key"));
    }
}

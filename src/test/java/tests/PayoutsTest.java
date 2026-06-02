package tests;

import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import dataprovider.PayoutsDataProvider;
import endpoints.Payouts;
import helpers.PayoutsHelper;
import helpers.TestContext;
import org.testng.annotations.Test;
import specification.ResponseSpec;
import testbase.BaseClass;

public class PayoutsTest extends BaseClass {

    // ***************CREATE PAYOUT – POSITIVE*******************\\

    @Test(groups = { "unit" })
    public void TC_01_Create_Valid_Payout() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

        Map<String, Object> body = new HashMap<>();
        body.put("amount", 1000);
        body.put("currency", "usd");

        String payoutId = Payouts.createPayout(body)
                .then()
                .spec(ResponseSpec.OK())
                .body("amount", equalTo(1000))
                .body("currency", equalTo("usd"))
                .extract()
                .jsonPath()
                .getString("id");

        logger.info("Created Payout ID: {}", payoutId);
        TestContext.setPayoutId(payoutId);
    }

    // ***************RETRIEVE PAYOUT – POSITIVE*******************\\

    @Test(groups = { "unit" })
    public void TC_02_Retrieve_Payout() {
        String payoutId = TestContext.getPayoutId();
        if (payoutId == null) {
            payoutId = PayoutsHelper.createFallbackPayout();
            logger.info("Created fallback payout ID: {}", payoutId);
        }

        Payouts.retrievePayout(payoutId)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(payoutId));
    }

    // ***************CANCEL PAYOUT – POSITIVE*******************\\

    @Test(groups = { "unit" })
    public void TC_03_Cancel_Payout() {
        String payoutId = TestContext.getPayoutId();
        if (payoutId == null) {
            payoutId = PayoutsHelper.createFallbackPayout();
            logger.info("Created fallback payout ID: {}", payoutId);
        }

        Payouts.cancelPayout(payoutId)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(payoutId))
                .body("status", equalTo("canceled"));
    }

    // ***************CREATE PAYOUT – NEGATIVE & EDGE CASES*******************\\

    @Test(groups = { "unit" })
    public void TC_04_CreatePayout_NegativeAmount() {
        Map<String, Object> body = new HashMap<>();
        body.put("amount", -100);
        body.put("currency", "usd");

        Payouts.createPayout(body)
                .then()
                .spec(ResponseSpec.bad_request());
    }

    @Test(groups = { "unit" })
    public void TC_05_CreatePayout_ZeroAmount() {
        Map<String, Object> body = new HashMap<>();
        body.put("amount", 0);
        body.put("currency", "usd");

        Payouts.createPayout(body)
                .then()
                .spec(ResponseSpec.bad_request());
    }

    @Test(groups = { "unit" })
    public void TC_06_CreatePayout_InvalidCurrency() {
        Map<String, Object> body = new HashMap<>();
        body.put("amount", 1000);
        body.put("currency", "invalid_curr");

        Payouts.createPayout(body)
                .then()
                .spec(ResponseSpec.bad_request());
    }

    @Test(groups = {
            "unit" }, dataProvider = "invalidPayoutPayloads", dataProviderClass = PayoutsDataProvider.class)
    public void TC_07_CreatePayout_MissingRequiredFields(String testCaseName, Map<String, Object> body) {
        logger.info("Running invalid payout payload case: {}", testCaseName);

        Payouts.createPayout(body)
                .then()
                .spec(ResponseSpec.bad_request());
    }

    @Test(groups = { "unit" })
    public void TC_08_CreatePayout_InvalidAuth() {
        Map<String, Object> body = new HashMap<>();
        body.put("amount", 1000);
        body.put("currency", "usd");

        Payouts.createPayoutWithCustomAuth("sk_test_invalid_key_12345", body)
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));
    }

    @Test(groups = { "unit" })
    public void TC_09_CreatePayout_MissingAuth() {
        Map<String, Object> body = new HashMap<>();
        body.put("amount", 1000);
        body.put("currency", "usd");

        Payouts.createPayoutWithCustomAuth(null, body)
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("You did not provide an API key"));
    }

    // ***************RETRIEVE PAYOUT – NEGATIVE*******************\\

    @Test(groups = { "unit" }, dataProvider = "invalidPayoutIds", dataProviderClass = PayoutsDataProvider.class)
    public void TC_10_RetrievePayout_InvalidId(String testCaseName, String payoutId, String expectedErrorFragment) {
        logger.info("Running invalid payout ID case: {}", testCaseName);

        Payouts.retrievePayout(payoutId)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.message", containsString(expectedErrorFragment));
    }

    @Test(groups = { "unit" })
    public void TC_11_RetrievePayout_InvalidAuth() {
        Payouts.retrievePayoutWithCustomAuth("sk_test_invalid_key_12345", "po_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));
    }

    @Test(groups = { "unit" })
    public void TC_12_RetrievePayout_MissingAuth() {
        Payouts.retrievePayoutWithCustomAuth(null, "po_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("You did not provide an API key"));
    }

    // ***************CANCEL PAYOUT – NEGATIVE*******************\\

    @Test(groups = { "unit" })
    public void TC_13_CancelPayout_InvalidId() {
        Payouts.cancelPayout("po_invalid_id_12345")
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.message", containsString("No such payout"));
    }

    @Test(groups = { "unit" })
    public void TC_14_CancelPayout_InvalidAuth() {
        Payouts.cancelPayoutWithCustomAuth("sk_test_invalid_key_12345", "po_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));
    }

    @Test(groups = { "unit" })
    public void TC_15_CancelPayout_MissingAuth() {
        Payouts.cancelPayoutWithCustomAuth(null, "po_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("You did not provide an API key"));
    }
}

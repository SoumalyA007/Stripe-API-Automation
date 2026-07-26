package tests;

import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataprovider.PayoutsDataProvider;
import endpoints.PaymentIntent;
import endpoints.Payouts;
import helpers.PayoutsHelper;
import helpers.PojoValidator;
import helpers.TestContext;
import helpers.TransfersHelper;
import helpers.PaymentIntentHelper;
import io.restassured.response.Response;
import models.response.PayoutResponse;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import specification.ResponseSpec;
import testbase.BaseClass;

public class PayoutsTest extends BaseClass {

    List<String> fallbackPayoutIds = new ArrayList<>();
    // ***************CREATE PAYOUT – POSITIVE*******************\\

    @Test(groups = { "payout", "regression", "create_retrieve_cancel_payout", "marketplace_e2e", "smoke" })
    public void TC_01_Create_Valid_Payout() {

        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

        // Resolve the actual paid amount first, before building the request body
        int amountPaid = amount / 2;
        String paymentIntentId = TestContext.getPaymentIntentId();
        if (paymentIntentId != null) {
            amountPaid = PaymentIntent.retrievePaymentIntent(paymentIntentId)
                    .then()
                    .spec(ResponseSpec.OK())
                    .extract()
                    .jsonPath()
                    .getInt("amount_received");
            logger.info("amount_received from PaymentIntent {}: {}", paymentIntentId, amountPaid);
        } else {
            // Standalone run: connected account has 0 balance. We must fund it first
            // via a fallback transfer, which uses source_transaction and succeeds.
            logger.info("No PaymentIntent in context. Funding connected account via fallback transfer...");
            TransfersHelper.createFallbackTransfer();
            logger.info("Connected account successfully funded.");
        }

        // Build the body with the resolved amount
        Map<String, Object> body = new HashMap<>();
        body.put("amount", amountPaid);
        body.put("currency", "usd");

        Map<String, String> headers = new HashMap<>();
        headers.put("Stripe-Account", p.getProperty("merchant_account_id"));

        Response resp = Payouts.createPayout(body, headers);
        String payoutId = resp.then()
                .spec(ResponseSpec.OK())
                .body("amount", equalTo(amountPaid))
                .body("currency", equalTo("usd"))
                .extract()
                .jsonPath()
                .getString("id");

        PayoutResponse payoutResponse = resp.as(PayoutResponse.class);
        PojoValidator.validate(payoutResponse);
        logger.info("POJO validation passed for payout: {}", payoutId);

        logger.info("Created Payout ID: {}", payoutId);
        TestContext.setPayoutId(payoutId);
    }

    // ***************RETRIEVE PAYOUT – POSITIVE*******************\\

    @Test(groups = { "payout", "regression", "create_retrieve_cancel_payout" })
    public void TC_02_Retrieve_Payout() {
        String payoutId = TestContext.getPayoutId();
        if (payoutId == null) {
            payoutId = PayoutsHelper.createFallbackPayout();
            logger.info("Created fallback payout ID: {}", payoutId);
            fallbackPayoutIds.add(payoutId);
        }

        Response resp = Payouts.retrievePayout(payoutId);
        resp.then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(payoutId));

        PayoutResponse payoutResponse = resp.as(PayoutResponse.class);
        PojoValidator.validate(payoutResponse);
        logger.info("POJO validation passed for retrieved payout: {}", payoutId);
    }

    // ***************CANCEL PAYOUT – POSITIVE*******************\\

    @Test(groups = { "payout", "regression", "create_retrieve_cancel_payout" })
    public void TC_03_Cancel_Payout() {
        String payoutId = TestContext.getPayoutId();
        if (payoutId == null) {
            payoutId = PayoutsHelper.createFallbackPayout();
            logger.info("Created fallback payout ID: {}", payoutId);
            fallbackPayoutIds.add(payoutId);
        }

        Payouts.cancelPayout(payoutId)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(payoutId))
                .body("status", equalTo("canceled"));
    }

    // ***************CREATE PAYOUT – NEGATIVE & EDGE CASES*******************\\

    @Test(groups = { "payout", "negative", "regression" })
    public void TC_04_CreatePayout_NegativeAmount() {
        logger.info("Testing create payout with negative amount");
        Map<String, Object> body = new HashMap<>();
        body.put("amount", -100);
        body.put("currency", "usd");

        Payouts.createPayout(body)
                .then()
                .spec(ResponseSpec.bad_request());

        logger.info("Successfully verified negative amount validation");
    }

    @Test(groups = { "payout", "negative", "regression" })
    public void TC_05_CreatePayout_ZeroAmount() {
        logger.info("Testing create payout with zero amount");
        Map<String, Object> body = new HashMap<>();
        body.put("amount", 0);
        body.put("currency", "usd");

        Payouts.createPayout(body)
                .then()
                .spec(ResponseSpec.bad_request());

        logger.info("Successfully verified zero amount validation");
    }

    @Test(groups = { "payout", "negative", "regression" })
    public void TC_06_CreatePayout_InvalidCurrency() {
        logger.info("Testing create payout with invalid currency");
        Map<String, Object> body = new HashMap<>();
        body.put("amount", 1000);
        body.put("currency", "invalid_curr");

        Payouts.createPayout(body)
                .then()
                .spec(ResponseSpec.bad_request());

        logger.info("Successfully verified invalid currency validation");
    }

    @Test(groups = { "payout", "negative",
            "regression" }, dataProvider = "invalidPayoutPayloads", dataProviderClass = PayoutsDataProvider.class)
    public void TC_07_CreatePayout_MissingRequiredFields(String testCaseName, Map<String, Object> body) {
        logger.info("Running invalid payout payload case: {}", testCaseName);

        Payouts.createPayout(body)
                .then()
                .spec(ResponseSpec.bad_request());

        logger.info("Successfully verified missing field validation for: {}", testCaseName);
    }

    @Test(groups = { "payout", "negative", "auth", "regression" })
    public void TC_08_CreatePayout_InvalidAuth() {
        logger.info("Testing create payout with invalid auth key");
        Map<String, Object> body = new HashMap<>();
        body.put("amount", amount / 2);
        body.put("currency", "usd");

        Payouts.createPayoutWithCustomAuth("sk_test_invalid_key_12345", body)
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));

        logger.info("Successfully verified unauthorized response for invalid auth");
    }

    @Test(groups = { "payout", "negative", "auth", "regression" })
    public void TC_09_CreatePayout_MissingAuth() {
        logger.info("Testing create payout with missing auth key");
        Map<String, Object> body = new HashMap<>();
        body.put("amount", amount / 2);
        body.put("currency", "usd");

        Payouts.createPayoutWithCustomAuth(null, body)
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("You did not provide an API key"));

        logger.info("Successfully verified unauthorized response for missing auth");
    }

    // ***************RETRIEVE PAYOUT – NEGATIVE*******************\\

    @Test(groups = { "payout", "negative",
            "regression" }, dataProvider = "invalidPayoutIds", dataProviderClass = PayoutsDataProvider.class)
    public void TC_10_RetrievePayout_InvalidId(String testCaseName, String payoutId, String expectedErrorFragment) {
        logger.info("Running invalid payout ID case: {} for ID: {}", testCaseName, payoutId);

        Payouts.retrievePayout(payoutId)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.message", containsString(expectedErrorFragment));

        logger.info("Successfully verified invalid payout ID retrieval failure: {}", expectedErrorFragment);
    }

    @Test(groups = { "payout", "negative", "auth", "regression" })
    public void TC_11_RetrievePayout_InvalidAuth() {
        logger.info("Testing retrieve payout with invalid auth key");
        Payouts.retrievePayoutWithCustomAuth("sk_test_invalid_key_12345", "po_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));

        logger.info("Successfully verified unauthorized response for retrieve payout invalid auth");
    }

    @Test(groups = { "payout", "negative", "auth", "regression" })
    public void TC_12_RetrievePayout_MissingAuth() {
        logger.info("Testing retrieve payout with missing auth key");
        Payouts.retrievePayoutWithCustomAuth(null, "po_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("You did not provide an API key"));

        logger.info("Successfully verified unauthorized response for retrieve payout missing auth");
    }

    // ***************CANCEL PAYOUT – NEGATIVE*******************\\

    @Test(groups = { "payout", "negative", "regression" })
    public void TC_13_CancelPayout_InvalidId() {
        logger.info("Testing cancel payout with invalid ID");
        Payouts.cancelPayout("po_invalid_id_12345")
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.message", containsString("No such payout"));

        logger.info("Successfully verified cancel payout invalid ID failure");
    }

    @Test(groups = { "payout", "negative", "auth", "regression" })
    public void TC_14_CancelPayout_InvalidAuth() {
        logger.info("Testing cancel payout with invalid auth key");
        Payouts.cancelPayoutWithCustomAuth("sk_test_invalid_key_12345", "po_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));

        logger.info("Successfully verified unauthorized response for cancel payout invalid auth");
    }

    @Test(groups = { "payout", "negative", "auth", "regression" })
    public void TC_15_CancelPayout_MissingAuth() {
        logger.info("Testing cancel payout with missing auth key");
        Payouts.cancelPayoutWithCustomAuth(null, "po_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("You did not provide an API key"));

        logger.info("Successfully verified unauthorized response for cancel payout missing auth");
    }

    @Test(groups = { "idempotent_test" })
    public void TC_16_positive_Idempotent_CreatePayout() {
        logger.info("Testing idempotent create payout");

        // Fund the platform's available balance first so the payout has sufficient
        // funds
        logger.info("Funding platform account available balance...");
        PaymentIntentHelper.createFallbackPaymentIntent(true);
        logger.info("Platform account balance successfully funded.");

        Map<String, Object> body = new HashMap<>();
        body.put("amount", amount / 2);
        body.put("currency", "usd");

        Map<String, String> headers = new HashMap<>();
        String idempotencyKey = "payout_key_" + System.currentTimeMillis();
        headers.put("Idempotency-Key", idempotencyKey);
        logger.info("Using idempotency key: {}", idempotencyKey);

        Response firstResponse = Payouts.createPayout(body, headers)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .response();

        String firstPayoutId = firstResponse.jsonPath().getString("id");

        Response secondResponse = Payouts.createPayout(body, headers)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .response();

        String secondPayoutId = secondResponse.jsonPath().getString("id");

        org.testng.Assert.assertEquals(firstPayoutId, secondPayoutId);
        logger.info("Successfully verified idempotent create payout for ID: {}", firstPayoutId);
        fallbackPayoutIds.add(secondPayoutId);
    }

    // ***************CLEANUP*******************\\

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        logger.info("Running cleanup for PayoutsTest");
        logger.info("Cancelling {} fallback payout(s)", fallbackPayoutIds.size());
        for (String payoutId : fallbackPayoutIds) {
            try {
                logger.info("Cancelling payout ID: {}", payoutId);
                Payouts.cancelPayout(payoutId);
            } catch (Exception e) {
                logger.warn("⚠️ Cleanup failed for payout: {}", payoutId);
            }
        }
        fallbackPayoutIds.clear();
    }
}

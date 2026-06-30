package tests;

import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import dataprovider.RefundDataProvider;
import endpoints.PaymentIntent;
import endpoints.Refunds;
import helpers.PaymentIntentHelper;
import helpers.RefundHelper;
import helpers.PojoValidator;
import helpers.TestContext;
import io.restassured.response.Response;
import models.response.RefundResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import specification.ResponseSpec;
import testbase.BaseClass;

public class RefundTests extends BaseClass {

        List<String> fallbackRefundIds = new ArrayList<>();
        List<String> createdPaymentIntentIds = new ArrayList<>();
        String negativeRefundPaymentIntetnId;

        // ***************CREATE REFUND – POSITIVE*******************\\

        // Full refund of paid amount
        @Test(groups = { "refund", "regression", "create_retrieve_refund", "marketplace_e2e" })
        public void TC_01_positive_Full_Refund() {
                logger.info("Testing positive full refund");
                String paymentIntentId = TestContext.getPaymentIntentId();
                logger.info("Fetched PaymentIntentId from context -->\t" + paymentIntentId);
                if (paymentIntentId == null) {
                        paymentIntentId = PaymentIntentHelper.createFallbackPaymentIntent(true);
                        logger.info("Created new paymentIntentId -->\t" + paymentIntentId);
                        TestContext.setPaymentIntentId(paymentIntentId);
                }

                Map<String, Object> body = new HashMap<>();
                body.put("payment_intent", paymentIntentId);
                body.put("amount", amount);

                Response refundResp = Refunds.createRefund(paymentIntentId, body);
                String refundId = refundResp.then()
                                .spec(ResponseSpec.OK())
                                .body("amount", equalTo(amount))
                                .body("currency", equalTo("usd"))
                                .body("payment_intent", equalTo(paymentIntentId))
                                .extract()
                                .jsonPath()
                                .getString("id");

                RefundResponse refundResponse = refundResp.as(RefundResponse.class);
                PojoValidator.validate(refundResponse);
                logger.info("POJO validation passed for refund: {}", refundId);

                logger.info("Refund Id -->\t" + refundId);

                logger.info("Successfully verified positive full refund for amount: {}", amount);

                TestContext.setRefundId(refundId);
        }

        // partial refund of paid amount
        @Test(groups = { "refund", "regression", "partialrefund_retrieve" })
        public void TC_02_positive_Partial_Refund() {
                logger.info("Testing positive partial refund");
                String paymentIntentId = TestContext.getPaymentIntentId();
                logger.info("Fetched PaymentIntentId from context -->\t" + paymentIntentId);

                if (paymentIntentId == null) {
                        paymentIntentId = PaymentIntentHelper.createFallbackPaymentIntent(true);
                        logger.info("Created new paymentIntentId -->\t" + paymentIntentId);
                        TestContext.setPaymentIntentId(paymentIntentId);
                }

                Map<String, Object> body = new HashMap<>();
                body.put("payment_intent", paymentIntentId);
                body.put("amount", amount / 2);

                String refundId = Refunds.createRefund(paymentIntentId, body)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("amount", equalTo(amount / 2))
                                .body("currency", equalTo("usd"))
                                .body("payment_intent", equalTo(paymentIntentId))
                                .extract()
                                .jsonPath()
                                .getString("id");

                logger.info("Refund Id -->\t" + refundId);

                logger.info("Successfully verified positive full refund for amount: {}", amount / 2);

                TestContext.setRefundId(refundId);
        }

        @Test(groups = { "refund", "regression", "create_retrieve_refund", "partialrefund_retrieve" })
        public void TC_02_Retrieve_Refund() {
                String refundId = TestContext.getRefundId();
                if (refundId == null) {
                        refundId = RefundHelper.createFallbackRefund();
                        logger.info("Created fallback refund ID: {}", refundId);
                        TestContext.setRefundId(refundId);
                }

                logger.info("Retrieving refund with ID: {}", refundId);
                Response resp = Refunds.retrieveRefund(refundId);
                resp.then()
                                .spec(ResponseSpec.OK())
                                .body("id", equalTo(refundId));

                RefundResponse refundResponse = resp.as(RefundResponse.class);
                PojoValidator.validate(refundResponse);
                logger.info("POJO validation passed for retrieved refund: {}", refundId);
                logger.info("Successfully retrieved refund with ID: {}", refundId);
        }

        // ***************CANCEL REFUND – POSITIVE*******************\\

        @Test(groups = { "refund", "regression", "cancel_refund" })
        public void TC_03_positive_CancelRefund() {
                logger.info("Testing positive cancel refund (requires_action flow)");

                // Build a refund that is in requires_action state
                String refundId = RefundHelper.createCancellableRefund();
                logger.info("Created requires_action refund ID: {}", refundId);

                // Verify the refund is actually in requires_action before cancelling
                Refunds.retrieveRefund(refundId)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("id", equalTo(refundId))
                                .body("status", equalTo("requires_action"));
                logger.info("Confirmed refund {} is in requires_action status", refundId);

                // Cancel the refund — only valid in requires_action state via API
                Response cancelResp = Refunds.cancelRefund(refundId);
                cancelResp.then()
                                .spec(ResponseSpec.OK())
                                .body("id", equalTo(refundId))
                                .body("status", equalTo("canceled"))
                                // Stripe docs: failure_reason and failure_balance_transaction
                                // are included on canceled refunds
                                .body("failure_reason", notNullValue())
                                .body("failure_balance_transaction", notNullValue());

                logger.info("Refund {} successfully cancelled. Status=canceled, failure_reason present.",
                                refundId);

                // Store for TC_15 double-cancel test so it doesn't need to create its own
                TestContext.setCanceledRefundId(refundId);
                logger.info("Stored canceled refund ID in context for TC_15: {}", refundId);
        }

        // ***************CREATE REFUND – NEGATIVE*******************\\

        // Create refund with an invalid payment_intent ID
        @Test(groups = { "refund", "negative", "regression" })
        public void TC_04_CreateRefund_InvalidPaymentIntentId() {
                logger.info("Testing create refund with invalid PaymentIntent ID");
                Map<String, Object> body = new HashMap<>();
                body.put("payment_intent", "pi_invalid_id_12345");
                body.put("amount", amount / 4);

                Refunds.createRefund("pi_invalid_id_12345", body)
                                .then()
                                .spec(ResponseSpec.bad_request());

                logger.info("Successfully verified invalid PaymentIntent ID refund failure");
        }

        // Create refund with the payment_intent field missing entirely
        @Test(groups = { "refund", "negative", "regression" })
        public void TC_05_CreateRefund_MissingPaymentIntent() {
                logger.info("Testing create refund with missing PaymentIntent");
                Map<String, Object> body = new HashMap<>();
                body.put("amount", amount / 4);

                Refunds.createRefund("", body)
                                .then()
                                .spec(ResponseSpec.bad_request());

                logger.info("Successfully verified missing PaymentIntent refund failure");
        }

        // Create refund whose amount exceeds the original payment charge
        @Test(groups = { "refund", "negative", "regression" })
        public void TC_06_CreateRefund_AmountExceedsCharged() {
                logger.info("Testing create refund exceeding original charged amount");
                if (negativeRefundPaymentIntetnId == null) {
                        negativeRefundPaymentIntetnId = PaymentIntentHelper.createFallbackPaymentIntent(true);
                        logger.info("Created paymentIntentId for negative tests: {}", negativeRefundPaymentIntetnId);
                        createdPaymentIntentIds.add(negativeRefundPaymentIntetnId);
                }

                Map<String, Object> body = new HashMap<>();
                body.put("payment_intent", negativeRefundPaymentIntetnId);
                body.put("amount", amount + 1000);

                Refunds.createRefund(negativeRefundPaymentIntetnId, body)
                                .then()
                                .spec(ResponseSpec.bad_request());

                logger.info("Successfully verified exceeds charged amount refund failure");
        }

        // Create refund with zero amount
        @Test(groups = { "refund", "negative", "regression" })
        public void TC_07_CreateRefund_ZeroAmount() {
                logger.info("Testing create refund with zero amount");
                if (negativeRefundPaymentIntetnId == null) {
                        negativeRefundPaymentIntetnId = PaymentIntentHelper.createFallbackPaymentIntent(true);
                        logger.info("Created paymentIntentId for negative tests: {}", negativeRefundPaymentIntetnId);
                        createdPaymentIntentIds.add(negativeRefundPaymentIntetnId);
                }
                Map<String, Object> body = new HashMap<>();
                body.put("payment_intent", negativeRefundPaymentIntetnId);
                body.put("amount", 0);

                Refunds.createRefund(negativeRefundPaymentIntetnId, body)
                                .then()
                                .spec(ResponseSpec.bad_request());

                logger.info("Successfully verified zero amount refund failure");
        }

        // Create refund with an invalid reason value – driven by DataProvider
        @Test(groups = { "refund", "negative",
                        "regression" }, dataProvider = "invalidRefundReasons", dataProviderClass = RefundDataProvider.class)
        public void TC_08_CreateRefund_InvalidReason(String testCaseName, String reason, String expectedErrorFragment) {
                logger.info("Testing create refund with invalid reason: {} -> {}", testCaseName, reason);
                // Fresh PI per iteration so the reason field is the only failure point
                if (negativeRefundPaymentIntetnId == null) {
                        negativeRefundPaymentIntetnId = PaymentIntentHelper.createFallbackPaymentIntent(true);
                        logger.info("Created paymentIntentId for negative tests: {}", negativeRefundPaymentIntetnId);
                        createdPaymentIntentIds.add(negativeRefundPaymentIntetnId);
                }
                logger.info("Created paymentIntentId for TC_08: {}", negativeRefundPaymentIntetnId);

                Map<String, Object> body = new HashMap<>();
                body.put("payment_intent", negativeRefundPaymentIntetnId);
                body.put("amount", amount / 4);
                body.put("reason", reason);

                Refunds.createRefund(negativeRefundPaymentIntetnId, body)
                                .then()
                                .spec(ResponseSpec.bad_request())
                                .body("error.message", containsString(expectedErrorFragment));
                logger.info("Successfully verified bad request and error fragment: {}", expectedErrorFragment);
        }

        // Create refund with an invalid auth token
        @Test(groups = { "refund", "negative", "auth", "regression" })
        public void TC_09_CreateRefund_InvalidAuth() {
                logger.info("Testing create refund with invalid auth token");
                Map<String, Object> body = new HashMap<>();
                body.put("payment_intent", "pi_any_id");
                body.put("amount", amount / 4);

                Refunds.createRefundWithCustomAuth("sk_test_invalid_key_12345", body)
                                .then()
                                .spec(ResponseSpec.Unauthorized())
                                .body("error.message", containsString("Invalid API Key provided"));
                logger.info("Successfully verified invalid auth error");
        }

        // Create refund with a missing auth token
        @Test(groups = { "refund", "negative", "auth", "regression" })
        public void TC_10_CreateRefund_MissingAuth() {
                logger.info("Testing create refund with missing auth token");
                Map<String, Object> body = new HashMap<>();
                body.put("payment_intent", "pi_any_id");
                body.put("amount", amount / 4);

                Refunds.createRefundWithCustomAuth(null, body)
                                .then()
                                .spec(ResponseSpec.Unauthorized())
                                .body("error.message", containsString("You did not provide an API key"));
                logger.info("Successfully verified missing auth error");
        }

        // ***************RETRIEVE REFUND – NEGATIVE*******************\\

        // Retrieve refund using invalid / garbage IDs – driven by DataProvider
        @Test(groups = { "refund", "negative",
                        "regression" }, dataProvider = "invalidRefundIds", dataProviderClass = RefundDataProvider.class)
        public void TC_11_RetrieveRefund_InvalidId(String testCaseName, String refundId,
                        String expectedErrorFragment) {
                logger.info("Testing retrieve refund with invalid/garbage ID: {} -> {}", testCaseName, refundId);
                Refunds.retrieveRefund(refundId)
                                .then()
                                .spec(ResponseSpec.not_found())
                                .body("error.message", containsString(expectedErrorFragment));
                logger.info("Successfully verified retrieve invalid refund error fragment: {}", expectedErrorFragment);
        }

        // Retrieve refund with an invalid auth token
        @Test(groups = { "refund", "negative", "auth", "regression" })
        public void TC_12_RetrieveRefund_InvalidAuth() {
                logger.info("Testing retrieve refund with invalid auth token");
                Refunds.retrieveRefundWithCustomAuth("sk_test_invalid_key_12345", "re_any_id")
                                .then()
                                .spec(ResponseSpec.Unauthorized())
                                .body("error.message", containsString("Invalid API Key provided"));
                logger.info("Successfully verified invalid auth error for retrieve refund");
        }

        // Retrieve refund with a missing auth token
        @Test(groups = { "refund", "negative", "auth", "regression" })
        public void TC_13_RetrieveRefund_MissingAuth() {
                logger.info("Testing retrieve refund with missing auth token");
                Refunds.retrieveRefundWithCustomAuth(null, "re_any_id")
                                .then()
                                .spec(ResponseSpec.Unauthorized())
                                .body("error.message", containsString("You did not provide an API key"));
                logger.info("Successfully verified missing auth error for retrieve refund");
        }

        // ***************CANCEL REFUND – NEGATIVE*******************\\

        // Cancel a refund with an invalid ID
        @Test(groups = { "refund", "negative", "regression" })
        public void TC_14_CancelRefund_InvalidId() {
                logger.info("Testing cancel refund with invalid ID");
                Refunds.cancelRefund("re_invalid_id_12345")
                                .then()
                                .spec(ResponseSpec.not_found());
                logger.info("Successfully verified cancel refund invalid ID failure");
        }

        // Cancel an already-cancelled refund (double-cancel edge case)
        @Test(groups = { "refund", "negative", "regression" })
        public void TC_15_CancelRefund_AlreadyCancelled() {
                logger.info("Testing cancel refund already cancelled (double-cancel)");

                // In a full flow, TC_03 already produced a cancelled refund in context.
                // In standalone mode, use the helper to create and cancel a fresh one.
                String refundId = TestContext.getCanceledRefundId();
                if (refundId == null) {
                        refundId = RefundHelper.createCancelledRefund();
                        logger.info("Created cancelled refund ID for TC_15 (standalone): {}", refundId);
                } else {
                        logger.info("Using already-cancelled refund ID from context: {}", refundId);
                }

                logger.info("Attempting second cancel for refund ID: {}", refundId);
                // Second cancel on an already-cancelled refund – must be rejected
                Refunds.cancelRefund(refundId)
                                .then()
                                .spec(ResponseSpec.bad_request());
                logger.info("Successfully verified second cancel (double-cancel) was rejected");
        }

        // Cancel refund with an invalid auth token
        @Test(groups = { "refund", "negative", "auth", "regression" })
        public void TC_16_CancelRefund_InvalidAuth() {
                logger.info("Testing cancel refund with invalid auth");
                Refunds.cancelRefundWithCustomAuth("sk_test_invalid_key_12345", "re_any_id")
                                .then()
                                .spec(ResponseSpec.Unauthorized())
                                .body("error.message", containsString("Invalid API Key provided"));
                logger.info("Successfully verified invalid auth error for cancel refund");
        }

        // Cancel refund with a missing auth token
        @Test(groups = { "refund", "negative", "auth", "regression" })
        public void TC_17_CancelRefund_MissingAuth() {
                logger.info("Testing cancel refund with missing auth");
                Refunds.cancelRefundWithCustomAuth(null, "re_any_id")
                                .then()
                                .spec(ResponseSpec.Unauthorized())
                                .body("error.message", containsString("You did not provide an API key"));
                logger.info("Successfully verified missing auth error for cancel refund");
        }

        @Test(groups = { "refund", "regression" })
        public void TC_18_positive_Idempotent_CreateRefund() {
                logger.info("Testing idempotent create refund");
                String paymentIntentId = PaymentIntentHelper.createFallbackPaymentIntent(true);
                createdPaymentIntentIds.add(paymentIntentId);
                logger.info("Created paymentIntentId for TC_18: {}", paymentIntentId);

                Map<String, Object> body = new HashMap<>();
                body.put("payment_intent", paymentIntentId);
                body.put("amount", amount / 2);

                Map<String, String> headers = new HashMap<>();
                String idempotencyKey = "ref_key_" + System.currentTimeMillis();
                headers.put("Idempotency-Key", idempotencyKey);
                logger.info("Using idempotency key: {}", idempotencyKey);

                Response firstResponse = Refunds.createRefund(paymentIntentId, body, headers)
                                .then()
                                .spec(ResponseSpec.OK())
                                .extract()
                                .response();

                String firstRefundId = firstResponse.jsonPath().getString("id");
                logger.info("First response Refund ID: {}", firstRefundId);

                Response secondResponse = Refunds.createRefund(paymentIntentId, body, headers)
                                .then()
                                .spec(ResponseSpec.OK())
                                .extract()
                                .response();

                String secondRefundId = secondResponse.jsonPath().getString("id");
                logger.info("Second response Refund ID: {}", secondRefundId);

                org.testng.Assert.assertEquals(firstRefundId, secondRefundId);
                logger.info("Verified refund IDs are equal (Idempotency success)");
        }

        @AfterClass(alwaysRun = true)
        public void cleanup() {
                // ── Refund cleanup (Stripe does not support deletion; log only) ──────────
                logger.info("Cleaning up {} fallback refund(s) created during standalone negative test runs",
                                fallbackRefundIds.size());
                for (String id : fallbackRefundIds) {
                        try {
                                // Refunds cannot be deleted via Stripe API; cancellation is the cleanup path.
                                // Only cancel if not already cancelled (TC_03 fallback path is already done).
                                logger.info("Skipping explicit cleanup for refund ID (already cancelled or non-deletable): {}",
                                                id);
                        } catch (Exception e) {
                                logger.error("Cleanup failed for refund ID: {}", id, e);
                        }
                }
                fallbackRefundIds.clear();

                // ── PaymentIntent cleanup ──────────────────────────────────────────────
                // Only cancels PIs created locally by this class (negativeRefundPaymentIntetnId
                // shared across TC_06/07/08, and TC_18's one-off PI).
                // PIs stored in TestContext (TC_01/TC_02 fallbacks) are excluded – they are
                // shared resources whose lifecycle is managed separately.
                logger.info("Cancelling {} PaymentIntent(s) created during RefundTests",
                                createdPaymentIntentIds.size());
                for (String piId : createdPaymentIntentIds) {
                        try {
                                Response cancelResponse = PaymentIntent.cancelPaymentIntent(piId, new HashMap<>());
                                int status = cancelResponse.getStatusCode();
                                if (status == 200) {
                                        logger.info("Cancelled PaymentIntent: {}", piId);
                                } else {
                                        // 400 payment_intent_unexpected_state = already succeeded/refunded; acceptable.
                                        logger.warn("Cancel returned HTTP {} for PaymentIntent {} – may already be in a terminal state",
                                                        status, piId);
                                }
                        } catch (Exception e) {
                                logger.error("Cleanup failed for PaymentIntent ID: {}", piId, e);
                        }
                }
                createdPaymentIntentIds.clear();
        }

}
package tests;

import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import dataprovider.RefundDataProvider;
import endpoints.Refunds;
import helpers.PaymentIntentHelper;
import helpers.RefundHelper;
import helpers.TestContext;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import specification.ResponseSpec;
import testbase.BaseClass;

public class RefundTests extends BaseClass {

        // ***************CREATE REFUND – POSITIVE*******************\\

        @Test(groups = { "refund", "regression", "refund_retrieve_cancel" })
        public void TC_01_positive_Full_Refund() {
                logger.info("Testing positive full refund");
                String paymentIntentId = TestContext.getPaymentIntentId();
                logger.info("Fetched PaymentIntentId from context -->\t" + paymentIntentId);
                boolean isFlow = true;

                if (paymentIntentId == null) {
                        paymentIntentId = PaymentIntentHelper.createFallbackPaymentIntent(true);
                        logger.info("Created new paymentIntentId -->\t" + paymentIntentId);

                        isFlow = false;
                }

                Map<String, Object> body = new HashMap<>();
                body.put("payment_intent", paymentIntentId);
                body.put("amount", amount);

                String refundId = Refunds.createRefund(paymentIntentId, body)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("amount", equalTo(amount))
                                .body("currency", equalTo("usd"))
                                .body("payment_intent", equalTo(paymentIntentId))
                                .extract()
                                .jsonPath()
                                .getString("id");

                logger.info("Refund Id -->\t" + refundId);

                logger.info("Successfully verified positive full refund for amount: {}", amount);

                if (isFlow) {
                        logger.info("Setting  RefundId if inside flow -->\t" + refundId);
                        TestContext.setRefundId(refundId);
                }
        }

        @Test(groups = { "refund", "regression", "partialrefund_retrieve_cancel" })
        public void TC_02_positive_Partial_Refund() {
                logger.info("Testing positive partial refund");
                String paymentIntentId = TestContext.getPaymentIntentId();
                logger.info("Fetched PaymentIntentId from context -->\t" + paymentIntentId);
                boolean isFlow = true;

                if (paymentIntentId == null) {
                        paymentIntentId = PaymentIntentHelper.createFallbackPaymentIntent(true);
                        logger.info("Created new paymentIntentId -->\t" + paymentIntentId);

                        isFlow = false;
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

                if (isFlow) {
                        logger.info("Setting  RefundId if inside flow -->\t" + refundId);
                        TestContext.setRefundId(refundId);
                }
        }

        @Test(groups = { "refund", "regression", "refund_retrieve_cancel", "partialrefund_retrieve_cancel" })
        public void TC_02_Retrieve_Refund() {
                String refundId = TestContext.getRefundId();
                if (refundId == null) {
                        refundId = RefundHelper.createFallbackRefund();
                        logger.info("Created fallback refund ID: {}", refundId);
                }

                logger.info("Retrieving refund with ID: {}", refundId);
                Refunds.retrieveRefund(refundId)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("id", equalTo(refundId));

                logger.info("Successfully retrieved refund with ID: {}", refundId);
        }

        @Test(groups = { "refund", "regression", "refund_retrieve_cancel", "partialrefund_retrieve_cancel" })
        public void TC_03_Cancel_Refund() {
                String refundId = TestContext.getRefundId();
                boolean isFlow = true;

                if (refundId == null) {
                        refundId = RefundHelper.createFallbackRefund();
                        logger.info("Created fallback refund ID: {}", refundId);
                        isFlow = false;
                }

                logger.info("Canceling refund with ID: {}", refundId);
                Refunds.cancelRefund(refundId)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("id", equalTo(refundId))
                                .body("status", equalTo("canceled"));

                if (isFlow) {
                        TestContext.setRefundId(refundId);
                }
                logger.info("Successfully canceled refund with ID: {}", refundId);
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
                String paymentIntentId = PaymentIntentHelper.createFallbackPaymentIntent(true);

                Map<String, Object> body = new HashMap<>();
                body.put("payment_intent", paymentIntentId);
                body.put("amount", amount + 1000);

                Refunds.createRefund(paymentIntentId, body)
                                .then()
                                .spec(ResponseSpec.bad_request());

                logger.info("Successfully verified exceeds charged amount refund failure");
        }

        // Create refund with zero amount
        @Test(groups = { "refund", "negative", "regression" })
        public void TC_07_CreateRefund_ZeroAmount() {
                logger.info("Testing create refund with zero amount");
                String paymentIntentId = PaymentIntentHelper.createFallbackPaymentIntent(true);

                Map<String, Object> body = new HashMap<>();
                body.put("payment_intent", paymentIntentId);
                body.put("amount", 0);

                Refunds.createRefund(paymentIntentId, body)
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
                String paymentIntentId = PaymentIntentHelper.createFallbackPaymentIntent(true);
                logger.info("Created paymentIntentId for TC_08: {}", paymentIntentId);

                Map<String, Object> body = new HashMap<>();
                body.put("payment_intent", paymentIntentId);
                body.put("amount", amount / 4);
                body.put("reason", reason);

                Refunds.createRefund(paymentIntentId, body)
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
                // Create a fresh refund specifically for this test
                String refundId = RefundHelper.createFallbackRefund();
                logger.info("Created refund ID for TC_15: {}", refundId);

                // First cancel – must succeed
                logger.info("Attempting first cancel for refund ID: {}", refundId);
                Refunds.cancelRefund(refundId)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("status", equalTo("canceled"));

                logger.info("First cancel successful. Attempting second cancel for refund ID: {}", refundId);
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

}
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

        @Test(groups = { "flow",
                        "unit" }, dependsOnMethods = "tests.PaymentIntentTests.TC_06_positive_Confirm_Payment_Intent")
        public void TC_01_Create_Valid_Refund() {
                logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

                Map<String, Object> body = new HashMap<>();
                body.put("amount", 1000);
                body.put("reason", "requested_by_customer");
                String paymentIntentId = TestContext.getPaymentIntentId();
                logger.info("Fetched PaymentIntentId from context -->\t" + paymentIntentId);
                boolean isFlow = true;

                if (paymentIntentId == null) {
                        paymentIntentId = PaymentIntentHelper.createFallbackPaymentIntent(true);
                        logger.info("Created new paymentIntentId -->\t" + paymentIntentId);

                        isFlow = false;
                }

                body.put("payment_intent", paymentIntentId);

                String refundId = Refunds.createRefund(paymentIntentId, body)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("amount", equalTo(1000))
                                .body("currency", equalTo("usd"))
                                .body("payment_intent", equalTo(paymentIntentId))
                                .extract()
                                .jsonPath()
                                .getString("id");

                logger.info("Refund Id -->\t" + refundId);

                if (isFlow) {
                        logger.info("Setting  RefundId if inside flow -->\t" + refundId);
                        TestContext.setRefundId(refundId);
                }

        }

        @Test(groups = { "unit" })
        public void TC_02_Retrieve_Refund() {

                String refundId = TestContext.getRefundId();

                if (refundId == null) {
                        refundId = RefundHelper.createFallbackRefund();
                }

                Refunds.retrieveRefund(refundId)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("id", equalTo(refundId));

        }

        @Test(groups = { "unit" })
        public void TC_03_Cancel_Refund() {

                String refundId = TestContext.getRefundId();
                boolean isFlow = true;

                if (refundId == null) {
                        refundId = RefundHelper.createFallbackRefund();
                        isFlow = false;
                }

                Refunds.cancelRefund(refundId)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("id", equalTo(refundId))
                                .body("status", equalTo("canceled"));

                if (isFlow) {
                        TestContext.setRefundId(refundId);
                }

        }

        // ***************CREATE REFUND – NEGATIVE*******************\\

        // Create refund with an invalid payment_intent ID
        @Test(groups = { "unit" })
        public void TC_04_CreateRefund_InvalidPaymentIntentId() {

                Map<String, Object> body = new HashMap<>();
                body.put("payment_intent", "pi_invalid_id_12345");
                body.put("amount", 500);

                Refunds.createRefund("pi_invalid_id_12345", body)
                                .then()
                                .spec(ResponseSpec.bad_request());

        }

        // Create refund with the payment_intent field missing entirely
        @Test(groups = { "unit" })
        public void TC_05_CreateRefund_MissingPaymentIntent() {

                Map<String, Object> body = new HashMap<>();
                body.put("amount", 500);
                // payment_intent intentionally omitted

                Refunds.createRefund("", body)
                                .then()
                                .spec(ResponseSpec.bad_request());

        }

        // Create refund whose amount exceeds the original payment charge
        @Test(groups = { "unit" })
        public void TC_06_CreateRefund_AmountExceedsCharged() {

                // Always create a fresh PI (amount = 2000) for this scenario
                String paymentIntentId = PaymentIntentHelper.createFallbackPaymentIntent(true);

                // Attempt to refund 9999 against a 2000 charge – must be rejected
                Map<String, Object> body = new HashMap<>();
                body.put("payment_intent", paymentIntentId);
                body.put("amount", 9999);

                Refunds.createRefund(paymentIntentId, body)
                                .then()
                                .spec(ResponseSpec.bad_request());

        }

        // Create refund with zero amount
        @Test(groups = { "unit" })
        public void TC_07_CreateRefund_ZeroAmount() {

                String paymentIntentId = PaymentIntentHelper.createFallbackPaymentIntent(true);

                Map<String, Object> body = new HashMap<>();
                body.put("payment_intent", paymentIntentId);
                body.put("amount", 0);

                Refunds.createRefund(paymentIntentId, body)
                                .then()
                                .spec(ResponseSpec.bad_request());

        }

        // Create refund with an invalid reason value – driven by DataProvider
        @Test(groups = { "unit" }, dataProvider = "invalidRefundReasons", dataProviderClass = RefundDataProvider.class)
        public void TC_08_CreateRefund_InvalidReason(String testCaseName, String reason, String expectedErrorFragment) {

                // Fresh PI per iteration so the reason field is the only failure point
                String paymentIntentId = PaymentIntentHelper.createFallbackPaymentIntent(true);

                Map<String, Object> body = new HashMap<>();
                body.put("payment_intent", paymentIntentId);
                body.put("amount", 500);
                body.put("reason", reason);

                Refunds.createRefund(paymentIntentId, body)
                                .then()
                                .spec(ResponseSpec.bad_request())
                                .body("error.message", containsString(expectedErrorFragment));

        }

        // Create refund with an invalid auth token
        @Test(groups = { "unit" })
        public void TC_09_CreateRefund_InvalidAuth() {

                Map<String, Object> body = new HashMap<>();
                body.put("payment_intent", "pi_any_id");
                body.put("amount", 500);

                Refunds.createRefundWithCustomAuth("sk_test_invalid_key_12345", body)
                                .then()
                                .spec(ResponseSpec.Unauthorized())
                                .body("error.message", containsString("Invalid API Key provided"));

        }

        // Create refund with a missing auth token
        @Test(groups = { "unit" })
        public void TC_10_CreateRefund_MissingAuth() {

                Map<String, Object> body = new HashMap<>();
                body.put("payment_intent", "pi_any_id");
                body.put("amount", 500);

                Refunds.createRefundWithCustomAuth(null, body)
                                .then()
                                .spec(ResponseSpec.Unauthorized())
                                .body("error.message", containsString("You did not provide an API key"));

        }

        // ***************RETRIEVE REFUND – NEGATIVE*******************\\

        // Retrieve refund using invalid / garbage IDs – driven by DataProvider
        @Test(groups = { "unit" }, dataProvider = "invalidRefundIds", dataProviderClass = RefundDataProvider.class)
        public void TC_11_RetrieveRefund_InvalidId(String testCaseName, String refundId,
                        String expectedErrorFragment) {

                Refunds.retrieveRefund(refundId)
                                .then()
                                .spec(ResponseSpec.not_found())
                                .body("error.message", containsString(expectedErrorFragment));

        }

        // Retrieve refund with an invalid auth token
        @Test(groups = { "unit" })
        public void TC_12_RetrieveRefund_InvalidAuth() {

                Refunds.retrieveRefundWithCustomAuth("sk_test_invalid_key_12345", "re_any_id")
                                .then()
                                .spec(ResponseSpec.Unauthorized())
                                .body("error.message", containsString("Invalid API Key provided"));

        }

        // Retrieve refund with a missing auth token
        @Test(groups = { "unit" })
        public void TC_13_RetrieveRefund_MissingAuth() {

                Refunds.retrieveRefundWithCustomAuth(null, "re_any_id")
                                .then()
                                .spec(ResponseSpec.Unauthorized())
                                .body("error.message", containsString("You did not provide an API key"));

        }

        // ***************CANCEL REFUND – NEGATIVE*******************\\

        // Cancel a refund with an invalid ID
        @Test(groups = { "unit" })
        public void TC_14_CancelRefund_InvalidId() {

                Refunds.cancelRefund("re_invalid_id_12345")
                                .then()
                                .spec(ResponseSpec.not_found());

        }

        // Cancel an already-cancelled refund (double-cancel edge case)
        @Test(groups = { "unit" })
        public void TC_15_CancelRefund_AlreadyCancelled() {

                // Create a fresh refund specifically for this test
                String refundId = RefundHelper.createFallbackRefund();

                // First cancel – must succeed
                Refunds.cancelRefund(refundId)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("status", equalTo("canceled"));

                // Second cancel on an already-cancelled refund – must be rejected
                Refunds.cancelRefund(refundId)
                                .then()
                                .spec(ResponseSpec.bad_request());

        }

        // Cancel refund with an invalid auth token
        @Test(groups = { "unit" })
        public void TC_16_CancelRefund_InvalidAuth() {

                Refunds.cancelRefundWithCustomAuth("sk_test_invalid_key_12345", "re_any_id")
                                .then()
                                .spec(ResponseSpec.Unauthorized())
                                .body("error.message", containsString("Invalid API Key provided"));

        }

        // Cancel refund with a missing auth token
        @Test(groups = { "unit" })
        public void TC_17_CancelRefund_MissingAuth() {

                Refunds.cancelRefundWithCustomAuth(null, "re_any_id")
                                .then()
                                .spec(ResponseSpec.Unauthorized())
                                .body("error.message", containsString("You did not provide an API key"));

        }

}
package tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataprovider.PaymentIntentDataProvider;

import org.testng.Assert;
import org.testng.annotations.Test;

import specification.ResponseSpec;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import endpoints.PaymentIntent;
import helpers.CustomersHelper;
import helpers.PaymentIntentHelper;
import helpers.PaymentMethodsHelper;
import helpers.TestContext;
import io.restassured.response.Response;

import testbase.BaseClass;

public class PaymentIntentTests extends BaseClass {

        List<String> paymethodIds = new ArrayList<>();

        // Creating paymentIntent
        @Test(groups = { "payment_intent", "regression", "create_cancel_paymentIntent",
                        "create_confirm_paymentIntent" })
        public void TC_01_positive_Create_Payment_Intent() {
                logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

                String paymentMethodId = TestContext.getPaymentMethodId();
                logger.info("Intial paymentMethodId fetch --> \t" + paymentMethodId);

                // Fallback for standalone run: create a temporary valid payment method if none
                // exists
                if (paymentMethodId == null) {
                        paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod();
                        logger.info("Inside IF block paymentMethodId fetch when it does not exist --> \t"
                                        + paymentMethodId);
                }

                Map<String, Object> body = new HashMap<>();
                body.put("amount", amount);
                body.put("currency", "usd");
                body.put("payment_method", paymentMethodId);

                // Standard user flows often require the payment intent linked to the customer
                String customerId = TestContext.getCustomerId();
                logger.info("Fetched customerId from context --> \t" + customerId);

                if (customerId == null) {
                        customerId = CustomersHelper.createCustomer();
                        TestContext.setCustomerId(customerId);
                }
                body.put("customer", customerId);

                body.put("automatic_payment_methods[enabled]", true);

                String paymentIntentId = PaymentIntent.createPaymentIntent(body)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("amount", equalTo(amount))
                                .body("currency", equalTo("usd"))
                                .body("automatic_payment_methods.enabled", equalTo(true))
                                .body("object", equalTo("payment_intent"))
                                .body("status", anyOf(equalTo("requires_payment_method"),
                                                equalTo("requires_confirmation")))
                                .extract()
                                .jsonPath()
                                .getString("id");

                logger.info("Created paymentIntentId --> \t" + paymentIntentId);

                TestContext.setPaymentIntentId(paymentIntentId);
                logger.info("paymentIntentId set in context ;");

        }

        // Confirming paymentIntents with invalid payment methods
        @Test(groups = { "payment_intent", "negative",
                        "regression",
                        "invalid_card_payment_intent" }, dataProvider = "createInvalidPaymentMethod", dataProviderClass = PaymentIntentDataProvider.class)
        public void TC_02_negative_Create_Payment_Intent(String testCaseName, Map<String, Object> body,
                        String expectedErrorCode) {
                logger.info("Running negative create payment intent test: {}", testCaseName);
                String paymentMethodId = PaymentMethodsHelper.createPaymentMethod(body);
                paymethodIds.add(paymentMethodId);
                logger.info("Created invalid payment method ID: {}", paymentMethodId);

                Map<String, Object> intentBody = new HashMap<>();
                intentBody.put("amount", amount);
                intentBody.put("currency", "usd");
                intentBody.put("payment_method", paymentMethodId);
                // Customer is omitted because it's optional for payment intent negative tests,
                // reducing redundant API calls
                intentBody.put("confirm", true);
                intentBody.put("automatic_payment_methods[enabled]", true);
                intentBody.put("automatic_payment_methods[allow_redirects]", "never");

                logger.info("Creating payment intent and confirming with invalid payment method");
                PaymentIntent.createPaymentIntent(intentBody)
                                .then()
                                .spec(ResponseSpec.request_failed())
                                .body("error.code", containsString(expectedErrorCode));
                logger.info("Successfully verified payment intent creation fails with error code: {}",
                                expectedErrorCode);
        }

        // Canceling paymentIntents with valid payment methods
        @Test(groups = { "payment_intent", "regression", "create_cancel_paymentIntent" })
        public void TC_03_positive_Cancel_Payment_Intent() {
                logger.info("Testing cancel payment intent");
                // Create an unconfirmed payment intent for cancellation testing
                String paymentIntentId = TestContext.getPaymentIntentId();
                logger.info("Fetched payment intent: {}", paymentIntentId);
                if (paymentIntentId == null) {
                        paymentIntentId = PaymentIntentHelper.createFallbackPaymentIntent(false);
                        logger.info("Created fallback payment intent: {}", paymentIntentId);
                }

                Map<String, Object> cancelBody = new HashMap<>();
                cancelBody.put("cancellation_reason", "requested_by_customer");

                logger.info("Canceling payment intent: {}", paymentIntentId);
                PaymentIntent.cancelPaymentIntent(paymentIntentId, cancelBody)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("status", equalTo("canceled"))
                                .body("cancellation_reason", equalTo("requested_by_customer"));
                logger.info("Successfully canceled payment intent");
                TestContext.setCanceledPaymentIntent(paymentIntentId);
        }

        // Canceling succeeded paymentIntents with valid payment methods
        @Test(groups = { "payment_intent", "negative", "regression" })
        public void TC_04_negative_Cancel_Succeeded_Payment_Intent() {
                logger.info("Testing cancel succeeded payment intent");

                String paymentIntentId = TestContext.getPaymentIntentId();

                // If context doesn't have an intent (standalone run), create a SUCCEEDED one.
                if (paymentIntentId == null) {
                        paymentIntentId = PaymentIntentHelper.createFallbackPaymentIntent(true);
                        logger.info("Created fallback succeeded payment intent: {}", paymentIntentId);
                        TestContext.setConfirmPaymentIntent(paymentIntentId);
                } else {
                        logger.info("Using active payment intent from context: {}", paymentIntentId);
                }

                Map<String, Object> cancelBody = new HashMap<>();
                cancelBody.put("cancellation_reason", "abandoned");

                logger.info("Attempting to cancel succeeded payment intent: {}", paymentIntentId);
                PaymentIntent.cancelPaymentIntent(paymentIntentId, cancelBody)
                                .then()
                                .spec(ResponseSpec.request_failed())
                                .body("error.code", equalTo("payment_intent_unexpected_state"));
                logger.info("Successfully verified cancel succeeded payment intent rejection");
        }

        // Canceling canceled paymentIntents
        @Test(groups = { "payment_intent", "negative", "regression" })
        public void TC_05_negative_Cancel_Already_Canceled_Payment_Intent() {
                logger.info("Testing cancel already canceled payment intent");
                // Unconfirmed so it's originally valid for cancellation
                String paymentIntentId = PaymentIntentHelper.createFallbackPaymentIntent(false);
                logger.info("Created fallback payment intent for cancellation: {}", paymentIntentId);

                Map<String, Object> cancelBody = new HashMap<>();
                cancelBody.put("cancellation_reason", "duplicate");

                // First cancellation succeeds
                logger.info("Performing first cancellation on ID: {}", paymentIntentId);
                PaymentIntent.cancelPaymentIntent(paymentIntentId, cancelBody)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("status", equalTo("canceled"));
                logger.info("First cancellation succeeded");

                // Second cancellation fails
                logger.info("Performing second cancellation on ID: {}", paymentIntentId);
                PaymentIntent.cancelPaymentIntent(paymentIntentId, cancelBody)
                                .then()
                                .spec(ResponseSpec.request_failed())
                                .body("error.code", equalTo("payment_intent_unexpected_state"));
                logger.info("Successfully verified double cancellation rejection");
                TestContext.setCanceledPaymentIntent(paymentIntentId);
        }

        // Confirming paymentIntent using the confirm endpoint
        @Test(groups = { "payment_intent", "regression", "create_confirm_paymentIntent" })
        public void TC_06_positive_Confirm_Payment_Intent() {
                logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

                String paymentIntentId = TestContext.getPaymentIntentId();
                logger.info("Intial payemtnIntenId fetch --> \t" + paymentIntentId);

                // If it doesn't exist, execute the whole prerequisite flow
                if (paymentIntentId == null) {
                        paymentIntentId = helpers.PaymentIntentHelper.createFallbackPaymentIntent(false);
                        logger.info("Inside IF block when intentId does not exist payemtnIntenId fetch --> \t"
                                        + paymentIntentId);
                }

                Map<String, Object> confirmBody = new HashMap<>();
                confirmBody.put("return_url", "https://example.com/return");

                logger.info("Confirming payment intent: {}", paymentIntentId);
                PaymentIntent.confirmPaymentIntent(paymentIntentId, confirmBody)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("status", equalTo("succeeded"))
                                .body("object", equalTo("payment_intent"));
                logger.info("Successfully confirmed payment intent");
        }

        @Test(groups = { "payment_intent", "regression" })
        public void TC_07_positive_Idempotent_Confirm_Payment_Intent() {
                logger.info("Testing idempotent confirmation of payment intent");
                // Create an unconfirmed intent
                String paymentIntentId = helpers.PaymentIntentHelper.createFallbackPaymentIntent(false);
                logger.info("Created fallback payment intent: {}", paymentIntentId);

                Map<String, Object> confirmBody = new HashMap<>();
                confirmBody.put("return_url", "https://example.com/return");

                Map<String, String> headers = new HashMap<>();
                String idempotencyKey = "key_" + System.currentTimeMillis();
                headers.put("Idempotency-Key", idempotencyKey);
                logger.info("Using idempotency key: {}", idempotencyKey);

                // First confirmation - Success
                logger.info("Sending first confirmation request");
                Response firstResponse = PaymentIntent.confirmPaymentIntent(paymentIntentId, confirmBody, headers)
                                .then()
                                .spec(ResponseSpec.OK())
                                .extract()
                                .response();

                String firstConfirmPaymentId = firstResponse.jsonPath().getString("id");
                String createdTimeFirstPayment = firstResponse.jsonPath().getString("created");
                logger.info("First confirmation succeeded. ID: {}, Created: {}", firstConfirmPaymentId,
                                createdTimeFirstPayment);

                // Second confirmation with same key - Still Success (Idempotent behavior)
                logger.info("Sending second confirmation request with same idempotency key");
                Response secondResponse = PaymentIntent.confirmPaymentIntent(paymentIntentId, confirmBody, headers)
                                .then()
                                .spec(ResponseSpec.OK())
                                .extract()
                                .response();

                String secondConfirmPaymentId = secondResponse.jsonPath().getString("id");
                String createdTimeSecondPayment = secondResponse.jsonPath().getString("created");
                logger.info("Second confirmation succeeded. ID: {}, Created: {}", secondConfirmPaymentId,
                                createdTimeSecondPayment);

                Assert.assertEquals(firstConfirmPaymentId, secondConfirmPaymentId);
                Assert.assertEquals(createdTimeFirstPayment, createdTimeSecondPayment);
                logger.info("Successfully verified idempotent confirmation returns matching response");
        }

        @Test(groups = { "payment_intent", "negative", "regression" })
        public void TC_08_negative_Confirm_Canceled_Payment_Intent() {
                logger.info("Testing confirmation of canceled payment intent");
                // Create a canceled intent
                String paymentIntentId = helpers.PaymentIntentHelper.createFallbackPaymentIntent(false);
                logger.info("Created fallback payment intent: {}", paymentIntentId);

                Map<String, Object> cancelBody = new HashMap<>();
                cancelBody.put("cancellation_reason", "abandoned");
                logger.info("Canceling payment intent: {}", paymentIntentId);
                PaymentIntent.cancelPaymentIntent(paymentIntentId, cancelBody)
                                .then()
                                .spec(ResponseSpec.OK());
                logger.info("Payment intent canceled");

                Map<String, Object> confirmBody = new HashMap<>();
                confirmBody.put("return_url", "https://example.com/return");

                // Confirming a canceled PI results in an error
                logger.info("Attempting to confirm canceled payment intent: {}", paymentIntentId);
                PaymentIntent.confirmPaymentIntent(paymentIntentId, confirmBody)
                                .then()
                                .spec(ResponseSpec.request_failed())
                                .body("error.code", equalTo("payment_intent_unexpected_state"));
                logger.info("Successfully verified confirmation of canceled payment intent is rejected");
        }

        @Test(groups = { "payment_intent", "negative",
                        "regression" }, dataProvider = "createInvalidPaymentMethod", dataProviderClass = PaymentIntentDataProvider.class)
        public void TC_09_negative_Confirm_Invalid_Payment_Method(String testCaseName, Map<String, Object> body,
                        String expectedErrorCode) {
                logger.info("Running confirm invalid payment method test: {}", testCaseName);
                String paymentMethodId = PaymentMethodsHelper.createPaymentMethod(body);
                logger.info("Created invalid payment method ID: {}", paymentMethodId);

                Map<String, Object> intentBody = new HashMap<>();
                intentBody.put("amount", amount);
                intentBody.put("currency", "usd");
                intentBody.put("payment_method", paymentMethodId);
                // Do not confirm during creation

                String paymentIntentId = PaymentIntent.createPaymentIntent(intentBody)
                                .then()
                                .spec(ResponseSpec.OK())
                                .extract()
                                .jsonPath()
                                .getString("id");
                logger.info("Created payment intent ID: {}", paymentIntentId);

                Map<String, Object> confirmBody = new HashMap<>();
                confirmBody.put("return_url", "https://example.com/return");

                // Confirming the intent should result in the specific card error
                logger.info("Confirming payment intent with invalid payment method");
                PaymentIntent.confirmPaymentIntent(paymentIntentId, confirmBody)
                                .then()
                                .spec(ResponseSpec.request_failed())
                                .body("error.code", containsString(expectedErrorCode));
                logger.info("Successfully verified invalid payment method confirmation fails with: {}",
                                expectedErrorCode);
        }

        @Test(groups = { "payment_intent", "negative", "regression" })
        public void TC_10_negative_Confirm_Succeeded_Payment_Intent() {
                logger.info("Testing confirmation of already succeeded payment intent");
                // Create an already succeeded intent
                String paymentIntentId = helpers.PaymentIntentHelper.createFallbackPaymentIntent(true);
                logger.info("Created fallback succeeded payment intent: {}", paymentIntentId);

                Map<String, Object> confirmBody = new HashMap<>();
                confirmBody.put("return_url", "https://example.com/return");

                // Confirming an already succeeded PI without the original idempotency key
                // results in an error
                logger.info("Attempting to confirm already succeeded payment intent: {}", paymentIntentId);
                PaymentIntent.confirmPaymentIntent(paymentIntentId, confirmBody)
                                .then()
                                .spec(ResponseSpec.request_failed())
                                .body("error.code", equalTo("payment_intent_unexpected_state"));
                logger.info("Successfully verified confirmation of succeeded payment intent is rejected");
        }

        @Test(groups = { "payment_intent", "negative", "regression" })
        public void TC_11_negative_Confirm_Without_Payment_Method() {
                logger.info("Testing confirmation of payment intent without payment method");
                // Create an intent without any payment method attached
                Map<String, Object> intentBody = new HashMap<>();
                intentBody.put("amount", amount);
                intentBody.put("currency", "usd");

                String paymentIntentId = PaymentIntent.createPaymentIntent(intentBody)
                                .then()
                                .spec(ResponseSpec.OK())
                                .extract()
                                .jsonPath()
                                .getString("id");
                logger.info("Created payment intent ID without payment method: {}", paymentIntentId);

                Map<String, Object> confirmBody = new HashMap<>();
                confirmBody.put("return_url", "https://example.com/return");

                // Trying to confirm it without providing a payment method
                logger.info("Attempting to confirm payment intent: {}", paymentIntentId);
                PaymentIntent.confirmPaymentIntent(paymentIntentId, confirmBody)
                                .then()
                                .spec(ResponseSpec.request_failed())
                                // In Stripe, you cannot confirm a PI if there's no payment method attached
                                .body("error.code", equalTo("payment_intent_unexpected_state"))
                                .body("error.message", containsString("payment_method"));
                logger.info("Successfully verified confirmation without payment method is rejected");
        }

}
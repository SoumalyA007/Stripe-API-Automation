package tests;

import java.util.HashMap;
import java.util.Map;

import dataprovider.PaymentIntentDataProvider;

import org.testng.Assert;
import org.testng.annotations.Test;

import specification.ResponseSpec;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import endpoints.PaymentIntent;

import helpers.PaymentMethodsHelper;
import helpers.TestContext;
import io.restassured.response.Response;

public class PaymentIntentTests {

    @Test
    public void TC_01_positive_Create_Payment_Intent() {

        String paymentMethodId = TestContext.getPaymentMethodId();

        boolean doesPaymentMethodIdExist = true;

        // Fallback for standalone run: create a temporary valid payment method if none
        // exists
        if (paymentMethodId == null) {
            paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(false);
            doesPaymentMethodIdExist = false;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("amount", 2000);
        body.put("currency", "usd");
        body.put("payment_method", paymentMethodId);

        // Standard user flows often require the payment intent linked to the customer
        String customerId = TestContext.getCustomerId();
        if (customerId != null) {
            body.put("customer", customerId);
        }

        body.put("automatic_payment_methods[enabled]", true);

        String paymentIntentId = PaymentIntent.createPaymentIntent(body)
                .then()
                .spec(ResponseSpec.OK())
                .body("amount", equalTo(2000))
                .body("currency", equalTo("usd"))
                .body("automatic_payment_methods.enabled", equalTo(true))
                .body("object", equalTo("payment_intent"))
                .body("status", anyOf(equalTo("requires_payment_method"), equalTo("requires_confirmation")))
                .extract()
                .jsonPath()
                .getString("id");

        if (doesPaymentMethodIdExist) {
            TestContext.setPaymentIntentId(paymentIntentId);
        }

    }

    @Test(dataProvider = "createInvalidPaymentMethod", dataProviderClass = PaymentIntentDataProvider.class)
    public void TC_02_negative_Create_Payment_Intent(String testCaseName, Map<String, Object> body,
            String expectedErrorCode) {

        String paymentMethodId = PaymentMethodsHelper.createPaymentMethod(body);

        Map<String, Object> intentBody = new HashMap<>();
        intentBody.put("amount", 2000);
        intentBody.put("currency", "usd");
        intentBody.put("payment_method", paymentMethodId);
        // Customer is omitted because it's optional for payment intent negative tests,
        // reducing redundant API calls
        intentBody.put("confirm", true);
        intentBody.put("automatic_payment_methods[enabled]", true);
        intentBody.put("automatic_payment_methods[allow_redirects]", "never");

        PaymentIntent.createPaymentIntent(intentBody)
                .then()
                .spec(ResponseSpec.request_failed())
                .body("error.code", containsString(expectedErrorCode));

    }

    @Test
    public void TC_03_positive_Cancel_Payment_Intent() {
        // Create an unconfirmed payment intent for cancellation testing
        String paymentIntentId = helpers.PaymentIntentHelper.createFallbackPaymentIntent(false);

        Map<String, Object> cancelBody = new HashMap<>();
        cancelBody.put("cancellation_reason", "requested_by_customer");

        PaymentIntent.cancelPaymentIntent(paymentIntentId, cancelBody)
                .then()
                .spec(ResponseSpec.OK())
                .body("status", equalTo("canceled"))
                .body("cancellation_reason", equalTo("requested_by_customer"));
    }

    @Test
    public void TC_04_negative_Cancel_Succeeded_Payment_Intent() {
        // This tests the negative scenario as you requested:
        // Trying to cancel an already succeeded payment intent.
        String paymentIntentId = TestContext.getPaymentIntentId();

        // If context doesn't have an intent (standalone run), create a SUCCEEDED one.
        if (paymentIntentId == null) {
            paymentIntentId = helpers.PaymentIntentHelper.createFallbackPaymentIntent(true);
        }

        Map<String, Object> cancelBody = new HashMap<>();
        cancelBody.put("cancellation_reason", "abandoned");

        PaymentIntent.cancelPaymentIntent(paymentIntentId, cancelBody)
                .then()
                .spec(ResponseSpec.request_failed())
                .body("error.code", equalTo("payment_intent_unexpected_state"));
    }

    @Test
    public void TC_05_negative_Cancel_Already_Canceled_Payment_Intent() {
        // Unconfirmed so it's originally valid for cancellation
        String paymentIntentId = helpers.PaymentIntentHelper.createFallbackPaymentIntent(false);

        Map<String, Object> cancelBody = new HashMap<>();
        cancelBody.put("cancellation_reason", "duplicate");

        // First cancellation succeeds
        PaymentIntent.cancelPaymentIntent(paymentIntentId, cancelBody)
                .then()
                .spec(ResponseSpec.OK())
                .body("status", equalTo("canceled"));

        // Second cancellation fails
        PaymentIntent.cancelPaymentIntent(paymentIntentId, cancelBody)
                .then()
                .spec(ResponseSpec.request_failed())
                .body("error.code", equalTo("payment_intent_unexpected_state"));
    }

    @Test
    public void TC_06_positive_Confirm_Payment_Intent() {
        String paymentIntentId = TestContext.getPaymentIntentId();

        // If it doesn't exist, execute the whole prerequisite flow
        if (paymentIntentId == null) {
            paymentIntentId = helpers.PaymentIntentHelper.createFallbackPaymentIntent(false);
        }

        Map<String, Object> confirmBody = new HashMap<>();
        confirmBody.put("return_url", "https://example.com/return");

        PaymentIntent.confirmPaymentIntent(paymentIntentId, confirmBody)
                .then()
                .spec(ResponseSpec.OK())
                .body("status", equalTo("succeeded"))
                .body("object", equalTo("payment_intent"));
    }

    @Test
    public void TC_07_positive_Idempotent_Confirm_Payment_Intent() {
        // Create an unconfirmed intent
        String paymentIntentId = helpers.PaymentIntentHelper.createFallbackPaymentIntent(false);

        Map<String, Object> confirmBody = new HashMap<>();
        confirmBody.put("return_url", "https://example.com/return");

        Map<String, String> headers = new HashMap<>();
        String idempotencyKey = "key_" + System.currentTimeMillis();
        headers.put("Idempotency-Key", idempotencyKey);

        // First confirmation - Success
        Response firstResponse = PaymentIntent.confirmPaymentIntent(paymentIntentId, confirmBody, headers)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .response();

        String firstConfirmPaymentId = firstResponse.jsonPath().getString("id");
        String createdTimeFirstPayment = firstResponse.jsonPath().getString("created");

        // Second confirmation with same key - Still Success (Idempotent behavior)
        Response secondResponse = PaymentIntent.confirmPaymentIntent(paymentIntentId, confirmBody, headers)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .response();

        String secondConfirmPaymentId = secondResponse.jsonPath().getString("id");
        String createdTimeSecondPayment = secondResponse.jsonPath().getString("created");

        Assert.assertEquals(firstConfirmPaymentId, secondConfirmPaymentId);
        Assert.assertEquals(createdTimeFirstPayment, createdTimeSecondPayment);

    }

    @Test
    public void TC_08_negative_Confirm_Canceled_Payment_Intent() {
        // Create a canceled intent
        String paymentIntentId = helpers.PaymentIntentHelper.createFallbackPaymentIntent(false);

        Map<String, Object> cancelBody = new HashMap<>();
        cancelBody.put("cancellation_reason", "abandoned");
        PaymentIntent.cancelPaymentIntent(paymentIntentId, cancelBody)
                .then()
                .spec(ResponseSpec.OK());

        Map<String, Object> confirmBody = new HashMap<>();
        confirmBody.put("return_url", "https://example.com/return");

        // Confirming a canceled PI results in an error
        PaymentIntent.confirmPaymentIntent(paymentIntentId, confirmBody)
                .then()
                .spec(ResponseSpec.request_failed())
                .body("error.code", equalTo("payment_intent_unexpected_state"));
    }

    @Test(dataProvider = "createInvalidPaymentMethod", dataProviderClass = PaymentIntentDataProvider.class)
    public void TC_09_negative_Confirm_Invalid_Payment_Method(String testCaseName, Map<String, Object> body,
            String expectedErrorCode) {

        String paymentMethodId = PaymentMethodsHelper.createPaymentMethod(body);

        Map<String, Object> intentBody = new HashMap<>();
        intentBody.put("amount", 2000);
        intentBody.put("currency", "usd");
        intentBody.put("payment_method", paymentMethodId);
        // Do not confirm during creation

        String paymentIntentId = PaymentIntent.createPaymentIntent(intentBody)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("id");

        Map<String, Object> confirmBody = new HashMap<>();
        confirmBody.put("return_url", "https://example.com/return");

        // Confirming the intent should result in the specific card error
        PaymentIntent.confirmPaymentIntent(paymentIntentId, confirmBody)
                .then()
                .spec(ResponseSpec.request_failed())
                .body("error.code", containsString(expectedErrorCode));
    }

    @Test
    public void TC_10_negative_Confirm_Succeeded_Payment_Intent() {
        // Create an already succeeded intent
        String paymentIntentId = helpers.PaymentIntentHelper.createFallbackPaymentIntent(true);

        Map<String, Object> confirmBody = new HashMap<>();
        confirmBody.put("return_url", "https://example.com/return");

        // Confirming an already succeeded PI without the original idempotency key
        // results in an error
        PaymentIntent.confirmPaymentIntent(paymentIntentId, confirmBody)
                .then()
                .spec(ResponseSpec.request_failed())
                .body("error.code", equalTo("payment_intent_unexpected_state"));
    }

    @Test
    public void TC_11_negative_Confirm_Without_Payment_Method() {
        // Create an intent without any payment method attached
        Map<String, Object> intentBody = new HashMap<>();
        intentBody.put("amount", 1500);
        intentBody.put("currency", "usd");

        String paymentIntentId = PaymentIntent.createPaymentIntent(intentBody)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("id");

        Map<String, Object> confirmBody = new HashMap<>();
        confirmBody.put("return_url", "https://example.com/return");

        // Trying to confirm it without providing a payment method
        PaymentIntent.confirmPaymentIntent(paymentIntentId, confirmBody)
                .then()
                .spec(ResponseSpec.request_failed())
                // In Stripe, you cannot confirm a PI if there's no payment method attached
                .body("error.code", equalTo("payment_intent_unexpected_state"))
                .body("error.message", containsString("payment_method"));
    }

}
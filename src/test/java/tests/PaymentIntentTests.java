package tests;

import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.Test;

import io.restassured.specification.ResponseSpecification;
import specification.ResponseSpec;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import endpoints.PaymentIntent;

import helpers.PaymentMethodsHelper;
import helpers.TestContext;

public class PaymentIntentTests {

    @Test
    public void TC_01_positive_Create_Payment_Intent() {

        String paymentMethodId = TestContext.getPaymentMethodId();

        // Fallback for standalone run: create a temporary valid payment method if none
        // exists
        if (paymentMethodId == null) {
            paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(false);
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

        PaymentIntent.createPaymentIntent(body)
                .then()
                .spec(ResponseSpec.OK())
                .body("amount", equalTo(2000))
                .body("currency", equalTo("usd"))
                .body("automatic_payment_methods.enabled", equalTo(true))
                .body("object", equalTo("payment_intent"))
                .body("status", anyOf(equalTo("requires_payment_method"), equalTo("requires_confirmation")));

    }

}

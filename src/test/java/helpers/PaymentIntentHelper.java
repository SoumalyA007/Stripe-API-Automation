package helpers;

import endpoints.PaymentIntent;
import specification.ResponseSpec;

import java.util.HashMap;
import java.util.Map;

public class PaymentIntentHelper {

    /**
     * Creates a fallback payment intent for standalone testing or prerequisite flows.
     * @param confirm Set to true to automatically confirm the payment intent upon creation (status = succeeded)
     * @return the created payment_intent ID
     */
    public static String createFallbackPaymentIntent(boolean confirm) {
        String paymentMethodId = TestContext.getPaymentMethodId();
        if (paymentMethodId == null) {
            paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(false);
        }

        Map<String, Object> body = new HashMap<>();
        // Default values for standard test flows
        body.put("amount", 2000);
        body.put("currency", "usd");
        body.put("payment_method", paymentMethodId);

        String customerId = TestContext.getCustomerId();
        if (customerId != null) {
            body.put("customer", customerId);
        }

        if (confirm) {
            body.put("confirm", true);
            body.put("automatic_payment_methods[enabled]", true);
            body.put("automatic_payment_methods[allow_redirects]", "never");
        }

        return PaymentIntent.createPaymentIntent(body)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("id");
    }
}

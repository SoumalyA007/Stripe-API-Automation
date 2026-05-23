package helpers;

import endpoints.paymentMethods;

import java.util.HashMap;
import java.util.Map;

public class PaymentMethodsHelper {

    public static String createValidPaymentMethod() {
        return createValidPaymentMethod(true);
    }

    public static String createValidPaymentMethod(boolean saveToContext) {
        Map<String, Object> method = new HashMap<>();
        method.put("type", "card");
        method.put("card[token]", "tok_visa");
        method.put("billing_details[email]", TestContext.getBillingEmail());
        method.put("billing_details[name]", TestContext.getBillingName());
        String paymentId = paymentMethods.createPaymentMethod(method)
                .then()
                .extract()
                .jsonPath()
                .getString("id");

        if (saveToContext) {
            TestContext.setPaymentMethodId(paymentId);
        }
        return paymentId;
    }

    public static String createInvalidPaymentMethod() {
        Map<String, Object> method = new HashMap<>();
        method.put("type", "card");
        method.put("card[token]", "tok_visa_chargeDeclinedInsufficientFunds");
        method.put("billing_details[email]", TestContext.getBillingEmail());
        method.put("billing_details[name]", TestContext.getBillingName());

        String paymentId = paymentMethods.createPaymentMethod(method)
                .then()
                .extract()
                .jsonPath()
                .getString("id");

        return paymentId;
    }

    public static String createPaymentMethod(Map<String, Object> method) {

        String paymentId = paymentMethods.createPaymentMethod(method)
                .then()
                .extract()
                .jsonPath()
                .getString("id");

        return paymentId;
    }

}
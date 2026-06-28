package helpers;

import endpoints.PaymentIntent;
import endpoints.Customer;
import specification.ResponseSpec;

import java.util.HashMap;
import java.util.Map;
import testbase.BaseClass;

public class PaymentIntentHelper {

    public static String createFallbackPaymentIntent(boolean confirm) {
        String paymentMethodId = TestContext.getPaymentMethodId();
        if (paymentMethodId == null) {
            paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(true);
        }

        Map<String, Object> body = new HashMap<>();
        // Default values for standard test flows
        body.put("amount", BaseClass.amount);
        body.put("currency", "usd");
        body.put("payment_method", paymentMethodId);

        String customerId = TestContext.getCustomerId();
        if (customerId != null) {
            customerId = CustomersHelper.createCustomer();
        }
        body.put("customer", customerId);

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

    public static String createCancelledPaymentIntent() {
        String paymentIntentId = PaymentIntentHelper.createFallbackPaymentIntent(false);

        Map<String, Object> cancelBody = new HashMap<>();
        cancelBody.put("cancellation_reason", "abandoned");
        PaymentIntent.cancelPaymentIntent(paymentIntentId, cancelBody);
        return paymentIntentId;
    }

    public static String createBankTransferPaymentIntentForCancellableRefund() {
        // Step 1: Create a fresh customer (customer_balance requires a customer)
        String customerId = CustomersHelper.createCustomer();

        // Step 2: Create a PaymentIntent with customer_balance / bank_transfer
        Map<String, Object> piBody = new HashMap<>();
        piBody.put("amount", BaseClass.amount);
        piBody.put("currency", "usd");
        piBody.put("customer", customerId);
        piBody.put("payment_method_types[]", "customer_balance");
        piBody.put("payment_method_data[type]", "customer_balance");
        piBody.put("payment_method_options[customer_balance][funding_type]", "bank_transfer");
        piBody.put("payment_method_options[customer_balance][bank_transfer][type]", "us_bank_transfer");
        piBody.put("confirm", true);

        String paymentIntentId = PaymentIntent.createPaymentIntent(piBody)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("id");

        // Step 3: Fund the customer's cash balance via Stripe test helpers so the PI
        // succeeds
        Map<String, Object> fundBody = new HashMap<>();
        fundBody.put("amount", BaseClass.amount);
        fundBody.put("currency", "usd");

        Customer.fundCashBalance(customerId, fundBody);

        return paymentIntentId;

    }
}

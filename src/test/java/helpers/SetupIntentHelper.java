package helpers;

import endpoints.Customer;
import endpoints.SetupIntent;
import endpoints.paymentMethods;
import io.restassured.response.Response;
import specification.ResponseSpec;

import java.util.HashMap;
import java.util.Map;

import com.github.javafaker.Faker;

public class SetupIntentHelper {

    private static final Faker faker = new Faker();

    /**
     * Creates a SetupIntent for off-session future payments.
     * If a customerId exists in context, it is attached automatically.
     *
     * @param saveToContext whether to save the created SetupIntent ID to
     *                      TestContext
     * @return the created setup_intent ID
     */
    public static String createSetupIntent(boolean saveToContext) {
        Map<String, Object> body = new HashMap<>();
        body.put("usage", "off_session");

        String customerId = TestContext.getCustomerId();
        if (customerId != null) {
            body.put("customer", customerId);
        }

        // Stripe test-mode requires specifying allowed payment method types
        body.put("payment_method_types[0]", "card");

        String setupIntentId = SetupIntent.createSetupIntent(body)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("id");

        if (saveToContext) {
            TestContext.setSetupIntentId(setupIntentId);
        }
        return setupIntentId;
    }

    /**
     * Creates a SetupIntent and confirms it with a valid payment method.
     * Useful as a prerequisite for tests that require a confirmed (succeeded)
     * SetupIntent.
     *
     * @return the confirmed setup_intent ID
     */
    public static String createAndConfirmSetupIntent() {
        // Ensure customer exists
        String customerId = TestContext.getCustomerId();
        if (customerId == null) {
            customerId = createFallbackCustomer();
        }

        // Ensure payment method exists
        String paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(false);

        // Attach payment method to customer
        Map<String, Object> attachBody = new HashMap<>();
        attachBody.put("customer", customerId);
        paymentMethods.attachPaymentMethod(paymentMethodId, attachBody);

        // Create SetupIntent
        Map<String, Object> siBody = new HashMap<>();
        siBody.put("customer", customerId);
        siBody.put("payment_method", paymentMethodId);
        siBody.put("usage", "off_session");
        siBody.put("payment_method_types[0]", "card");

        String setupIntentId = SetupIntent.createSetupIntent(siBody)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("id");

        // Confirm the SetupIntent
        Map<String, Object> confirmBody = new HashMap<>();
        confirmBody.put("payment_method", paymentMethodId);
        SetupIntent.confirmSetupIntent(setupIntentId, confirmBody)
                .then()
                .spec(ResponseSpec.OK());

        TestContext.setSetupIntentId(setupIntentId);
        return setupIntentId;
    }

    /**
     * Creates a fallback customer for standalone tests.
     *
     * @return the created customer ID
     */
    public static String createFallbackCustomer() {
        String name = faker.name().fullName();
        String email = name.replaceAll(" ", "") + "@test.com";
        Response resp = Customer.createCustomer(name, email, null);
        String customerId = resp.jsonPath().getString("id");
        TestContext.setCustomerId(customerId);
        TestContext.setBillingName(name);
        TestContext.setBillingEmail(email);
        return customerId;
    }
}

package helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javafaker.Faker;

import endpoints.Customer;
import endpoints.Price;
import endpoints.Product;
import endpoints.Subscription;
import endpoints.paymentMethods;
import io.restassured.response.Response;
import specification.ResponseSpec;

public class SubscriptionHelper {

    private static final Faker faker = new Faker();

    public static String createProduct(boolean saveToContext) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Automation Test Product - " + System.currentTimeMillis());
        body.put("type", "service");

        String productId = Product.createProduct(body)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("id");

        if (saveToContext) {
            TestContext.setProductId(productId);
        }
        return productId;
    }

    public static String createRecurringPrice(String productId, int unitAmount, String currency,
            String interval, boolean saveToContext) {
        Map<String, Object> body = new HashMap<>();
        body.put("product", productId);
        body.put("unit_amount", unitAmount);
        body.put("currency", currency);
        body.put("recurring[interval]", interval);

        String priceId = Price.createPrice(body)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("id");

        if (saveToContext) {
            TestContext.setPriceId(priceId);
        }
        return priceId;
    }

    public static String createSubscriptionReadyCustomer() {
        // 1. Create customer
        String name = faker.name().fullName();
        String email = name.replaceAll(" ", "") + "@test.com";
        Response custResp = Customer.createCustomer(name, email, null);
        String customerId = custResp.jsonPath().getString("id");
        TestContext.setCustomerId(customerId);
        TestContext.setBillingName(name);
        TestContext.setBillingEmail(email);

        // 2. Create payment method
        String paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(false);

        // 3. Attach payment method to customer
        Map<String, Object> attachBody = new HashMap<>();
        attachBody.put("customer", customerId);
        paymentMethods.attachPaymentMethod(paymentMethodId, attachBody);

        // 4. Set default payment method for invoices
        Map<String, String> metadata = null;
        Customer.updateCustomer(customerId,
                "invoice_settings[default_payment_method]", paymentMethodId, metadata);

        TestContext.setPaymentMethodId(paymentMethodId);
        TestContext.setSubscriptionPaymentMethodId(paymentMethodId);
        return customerId;
    }

    public static String createFullSubscription() {
        String productId = createProduct(true);
        String priceId = createRecurringPrice(productId, 1500, "usd", "month", true);
        String customerId = createSubscriptionReadyCustomer();

        Map<String, Object> subBody = new HashMap<>();
        subBody.put("customer", customerId);
        subBody.put("items[0][price]", priceId);

        String subscriptionId = Subscription.createSubscription(subBody)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("id");

        TestContext.setSubscriptionId(subscriptionId);
        return subscriptionId;
    }

    public static boolean hasNoPrices(String productId) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("product", productId);
        queryParams.put("limit", 1);

        Response resp = Price.listPrices(queryParams);
        resp.then().spec(ResponseSpec.OK());

        List<Map<String, Object>> prices = resp.jsonPath().getList("data");
        return prices.isEmpty();
    }
}

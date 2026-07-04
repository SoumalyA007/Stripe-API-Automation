package tests;

import dataprovider.SubscriptionDataProvider;
import endpoints.Customer;
import endpoints.Product;
import endpoints.Price;
import endpoints.Subscription;
import endpoints.paymentMethods;
import helpers.CustomersHelper;
import helpers.PaymentMethodsHelper;
import helpers.PojoValidator;
import helpers.SubscriptionHelper;
import helpers.TestContext;
import io.restassured.response.Response;
import models.response.PriceResponse;
import models.response.ProductResponse;
import models.response.SubscriptionResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import specification.ResponseSpec;
import testbase.BaseClass;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SubscriptionTests extends BaseClass {

    private List<String> productIdsToCleanup = new ArrayList<>();
    private List<String> customerIdsToCleanup = new ArrayList<>();
    private List<String> subscriptionIdsToCleanup = new ArrayList<>();

    // ═══════════════════════════════════════════════════════════════
    // ██ PRODUCT TESTS
    // ═══════════════════════════════════════════════════════════════

    @Test(groups = { "subscription", "regression", "crate_retrieve_delete_product", "subscription_e2e", "smoke" })
    public void TC_01_positive_Create_Product() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

        Map<String, Object> body = new HashMap<>();
        String name = "Test Product - " + System.currentTimeMillis();
        body.put("name", name);
        body.put("type", "service");
        body.put("description", "Product for automation testing");

        Response resp = Product.createProduct(body);
        String productId = resp.then()
                .spec(ResponseSpec.OK())
                .body("id", notNullValue())
                .body("name", equalTo(name))
                .body("type", equalTo("service"))
                .extract()
                .jsonPath()
                .getString("id");

        ProductResponse productResponse = resp.as(ProductResponse.class);
        PojoValidator.validate(productResponse);
        logger.info("POJO validation passed for product: {}", productId);

        productIdsToCleanup.add(productId);
        TestContext.setProductId(productId);
        logger.info("✅ Product created successfully: {}", productId);
    }

    @Test(groups = { "subscription",
            "regression",
            "crate_retrieve_delete_product" }, dependsOnMethods = "TC_01_positive_Create_Product", ignoreMissingDependencies = true)
    public void TC_02_positive_Retrieve_Product() {
        logger.info("Testing retrieve product");
        String productId = TestContext.getProductId();
        if (productId == null) {
            productId = SubscriptionHelper.createProduct(true);
            productIdsToCleanup.add(productId);
            logger.info("Created fallback product ID: {}", productId);
        }

        logger.info("Retrieving product ID: {}", productId);
        Product.retrieveProduct(productId)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(productId))
                .body("type", equalTo("service"));

        logger.info("✅ Product retrieved successfully: {}", productId);
    }

    @Test(groups = { "subscription", "regression", "crate_retrieve_delete_product" })
    public void TC_03_positive_Delete_Product() {
        logger.info("Testing delete product");
        String productId = TestContext.getProductId();
        if (productId == null) {
            productId = SubscriptionHelper.createProduct(true);
            productIdsToCleanup.add(productId);
            logger.info("Created fallback product ID: {}", productId);
        }
        logger.info("Created product ID for deletion: {}", productId);

        logger.info("Deleting product ID: {}", productId);
        Product.deleteProduct(productId)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(productId))
                .body("deleted", equalTo(true));

        logger.info("✅ Product deleted successfully: {}", productId);
        // NOTE: productId is NOT cleared from TestContext — it was deleted from Stripe
        // but clearing from context could break downstream reads in the same suite.
    }

    @Test(groups = { "subscription", "negative",
            "regression" }, dataProvider = "invalidProductBodies", dataProviderClass = SubscriptionDataProvider.class)
    public void TC_04_negative_Create_Product_Invalid(String testCaseName, Map<String, Object> body) {
        logger.info("Running invalid product body case: {}", testCaseName);

        Product.createProduct(body)
                .then()
                .spec(ResponseSpec.request_failed())
                .body("error.type", equalTo("invalid_request_error"));

        logger.info("✅ Correctly rejected invalid product body: {}", testCaseName);
    }

    // ═══════════════════════════════════════════════════════════════
    // ██ PRICE TESTS
    // ═══════════════════════════════════════════════════════════════

    @Test(groups = { "subscription",
            "regression",
            "subscription_e2e" }, dependsOnMethods = "TC_01_positive_Create_Product", ignoreMissingDependencies = true)
    public void TC_05_positive_Create_Price() {
        logger.info("Testing positive create price");
        String productId = TestContext.getProductId();
        if (productId == null) {
            productId = SubscriptionHelper.createProduct(true);
            productIdsToCleanup.add(productId);
            logger.info("Created fallback product ID: {}", productId);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("product", productId);
        body.put("unit_amount", amount);
        body.put("currency", "usd");
        body.put("recurring[interval]", "month");

        logger.info("Creating price for product: {}", productId);
        Response resp = Price.createPrice(body);
        String priceId = resp.then()
                .spec(ResponseSpec.OK())
                .body("id", notNullValue())
                .body("product", equalTo(productId))
                .body("unit_amount", equalTo(amount))
                .body("currency", equalTo("usd"))
                .body("recurring.interval", equalTo("month"))
                .extract()
                .jsonPath()
                .getString("id");

        PriceResponse priceResponse = resp.as(PriceResponse.class);
        PojoValidator.validate(priceResponse);
        logger.info("POJO validation passed for price: {}", priceId);

        TestContext.setPriceId(priceId);
        logger.info("✅ Price created successfully: {}", priceId);
    }

    @Test(groups = { "subscription",
            "regression" }, dataProvider = "subscriptionIntervals", dataProviderClass = SubscriptionDataProvider.class)
    public void TC_06_positive_Create_Price_Intervals(String planName, int amount, String currency, String interval) {
        logger.info("Creating price for: {}", planName);

        String productId = TestContext.getProductId();
        if (productId == null) {
            productId = SubscriptionHelper.createProduct(true);
            productIdsToCleanup.add(productId);
            logger.info("Created fallback product ID: {}", productId);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("product", productId);
        body.put("unit_amount", amount);
        body.put("currency", currency);
        body.put("recurring[interval]", interval);

        Price.createPrice(body)
                .then()
                .spec(ResponseSpec.OK())
                .body("unit_amount", equalTo(amount))
                .body("currency", equalTo(currency))
                .body("recurring.interval", equalTo(interval));

        logger.info("✅ Successfully created price with interval: {}", interval);
    }

    @Test(groups = { "subscription",
            "regression" }, dependsOnMethods = "TC_05_positive_Create_Price", ignoreMissingDependencies = true)
    public void TC_07_positive_Retrieve_Price() {
        logger.info("Testing retrieve price");
        String priceId = TestContext.getPriceId();
        if (priceId == null) {
            String productId = SubscriptionHelper.createProduct(true);
            productIdsToCleanup.add(productId);
            priceId = SubscriptionHelper.createRecurringPrice(productId, 1500, "usd", "month", true);
            logger.info("Created fallback price ID: {}", priceId);
        }

        logger.info("Retrieving price ID: {}", priceId);
        Price.retrievePrice(priceId)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(priceId));

        logger.info("✅ Price retrieved successfully: {}", priceId);
    }

    @Test(groups = { "subscription", "negative",
            "regression" }, dataProvider = "invalidPriceBodies", dataProviderClass = SubscriptionDataProvider.class)
    public void TC_08_negative_Create_Price_Invalid(String testCaseName, Map<String, Object> body) {
        logger.info("Running invalid price body case: {}", testCaseName);

        if ("Negative Amount".equals(testCaseName) || "Invalid Currency".equals(testCaseName)) {
            String productId = TestContext.getProductId();
            if (productId == null) {
                productId = SubscriptionHelper.createProduct(true);
                productIdsToCleanup.add(productId);
                logger.info("Created fallback product ID for invalid price test: {}", productId);
            }
            body.put("product", productId);
        }

        Price.createPrice(body)
                .then()
                .spec(ResponseSpec.request_failed())
                .body("error.type", equalTo("invalid_request_error"));

        logger.info("✅ Correctly rejected invalid price body: {}", testCaseName);
    }

    // ═══════════════════════════════════════════════════════════════
    // ██ SUBSCRIPTION TESTS
    // ═══════════════════════════════════════════════════════════════

    @Test(groups = { "subscription", "regression", "subscription_e2e", "smoke" })
    public void TC_09_positive_Create_Subscription() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));
        logger.info("Testing positive create subscription");

        String subscriptionCustomerId = TestContext.getSubscriptionCustomerId();
        if (subscriptionCustomerId == null) {
            subscriptionCustomerId = SubscriptionHelper.createSubscriptionReadyCustomer();
            customerIdsToCleanup.add(subscriptionCustomerId);
            logger.info("Created subscription ready customer ID: {}", subscriptionCustomerId);
        }

        String priceId = TestContext.getPriceId();
        if (priceId == null) {
            String productId = SubscriptionHelper.createProduct(true);
            productIdsToCleanup.add(productId);
            priceId = SubscriptionHelper.createRecurringPrice(productId, 1500, "usd", "month", true);
            logger.info("Created fallback price ID: {}", priceId);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("customer", subscriptionCustomerId);
        body.put("items[0][price]", priceId);

        logger.info("Creating subscription for customer: {} with price: {}", subscriptionCustomerId, priceId);
        Response resp = Subscription.createSubscription(body);
        String subId = resp.then()
                .spec(ResponseSpec.OK())
                .body("id", notNullValue())
                .body("customer", equalTo(subscriptionCustomerId))
                .body("items.data[0].price.id", equalTo(priceId))
                .body("status", equalTo("active"))
                .extract()
                .jsonPath()
                .getString("id");

        SubscriptionResponse subResponse = resp.as(SubscriptionResponse.class);
        PojoValidator.validate(subResponse);
        logger.info("POJO validation passed for subscription: {}", subId);

        subscriptionIdsToCleanup.add(subId);
        TestContext.setSubscriptionId(subId);
        logger.info("✅ Subscription created successfully: {}", subId);
    }

    @Test(groups = { "subscription",
            "regression",
            "subscription_e2e" }, dependsOnMethods = "TC_09_positive_Create_Subscription", ignoreMissingDependencies = true)
    public void TC_10_positive_Retrieve_Subscription() {
        logger.info("Testing retrieve subscription");
        String subId = TestContext.getSubscriptionId();
        if (subId == null) {
            subId = SubscriptionHelper.createFullSubscription();
            subscriptionIdsToCleanup.add(subId);
            logger.info("Created fallback subscription ID: {}", subId);
        }

        logger.info("Retrieving subscription ID: {}", subId);
        Response resp = Subscription.retrieveSubscription(subId);
        resp.then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(subId))
                .body("status", equalTo("active"));

        SubscriptionResponse subResponse = resp.as(SubscriptionResponse.class);
        PojoValidator.validate(subResponse);
        logger.info("POJO validation passed for retrieved subscription: {}", subId);
        logger.info("✅ Subscription retrieved successfully: {}", subId);
    }

    @Test(groups = { "subscription",
            "regression",
            "subscription_e2e" }, dataProvider = "subscriptionMetadataUpdates", dataProviderClass = SubscriptionDataProvider.class)
    public void TC_11_positive_Update_Subscription_Metadata(String key, String value) {
        logger.info("Testing update subscription metadata: {}={}", key, value);
        String subId = TestContext.getSubscriptionId();
        if (subId == null) {
            subId = SubscriptionHelper.createFullSubscription();
            subscriptionIdsToCleanup.add(subId);
            logger.info("Created fallback subscription ID: {}", subId);
        }

        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("metadata[" + key + "]", value);

        logger.info("Updating subscription ID: {}", subId);
        Subscription.updateSubscription(subId, updateBody)
                .then()
                .spec(ResponseSpec.OK())
                .body("metadata." + key, equalTo(value));

        logger.info("✅ Subscription metadata updated successfully: {}={}", key, value);
    }

    @Test(groups = { "subscription", "regression", "sanity" })
    public void TC_12_positive_List_Subscriptions() {
        logger.info("Testing list subscriptions");
        String subId = TestContext.getSubscriptionId();
        if (subId == null) {
            subId = SubscriptionHelper.createFullSubscription();
            subscriptionIdsToCleanup.add(subId);
            logger.info("Created fallback subscription ID: {}", subId);
        }

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("limit", 3);

        logger.info("Listing subscriptions");
        Subscription.listSubscriptions(queryParams)
                .then()
                .spec(ResponseSpec.OK())
                .body("object", equalTo("list"))
                .body("data.size()", greaterThanOrEqualTo(1))
                .body("data[0].object", equalTo("subscription"));

        logger.info("✅ Subscriptions listed successfully");
    }

    @Test(groups = { "subscription", "regression", "subscription_e2e" })
    public void TC_13_positive_Cancel_Subscription() {
        logger.info("Testing cancel subscription");
        String subId = TestContext.getSubscriptionId();
        if (subId == null) {
            subId = SubscriptionHelper.createFullSubscription();
            subscriptionIdsToCleanup.add(subId);
            logger.info("Created fallback subscription ID: {}", subId);
        }

        logger.info("Canceling subscription ID: {}", subId);
        Subscription.cancelSubscription(subId)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(subId))
                .body("status", equalTo("canceled"));

        TestContext.setCancelledSubscriptionId(subId);
        logger.info("✅ Subscription canceled successfully: {}", subId);
    }

    @Test(groups = { "subscription", "negative",
            "regression" }, dataProvider = "invalidSubscriptionIds", dataProviderClass = SubscriptionDataProvider.class)
    public void TC_14_negative_Retrieve_Invalid_Subscription(String invalidId) {
        logger.info("Retrieving invalid subscription: {}", invalidId);

        Subscription.retrieveSubscription(invalidId)
                .then()
                .spec(ResponseSpec.not_found());

        logger.info("✅ Correctly rejected invalid subscription ID retrieval: {}", invalidId);
    }

    @Test(groups = { "subscription", "negative", "regression" })
    public void TC_15_negative_Create_Subscription_Invalid_Customer() {
        logger.info("Testing create subscription with invalid customer");
        String priceId = TestContext.getPriceId();
        if (priceId == null) {
            String productId = SubscriptionHelper.createProduct(false);
            productIdsToCleanup.add(productId);
            priceId = SubscriptionHelper.createRecurringPrice(productId, 1500, "usd", "month", false);
            logger.info("Created fallback price ID: {}", priceId);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("customer", "cus_invalid_id_123");
        body.put("items[0][price]", priceId);

        logger.info("Attempting to create subscription with invalid customer");
        Subscription.createSubscription(body)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.message", containsString("No such customer"));

        logger.info("✅ Correctly rejected subscription creation for nonexistent customer");
    }

    @Test(groups = { "subscription", "negative", "regression" })
    public void TC_16_negative_Create_Subscription_Invalid_Price() {
        logger.info("Testing create subscription with invalid price");
        String customerId = TestContext.getCustomerId();
        if (customerId == null) {
            customerId = SubscriptionHelper.createSubscriptionReadyCustomer();
            customerIdsToCleanup.add(customerId);
            logger.info("Created subscription ready customer ID: {}", customerId);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("customer", customerId);
        body.put("items[0][price]", "price_invalid_id_123");

        logger.info("Attempting to create subscription with invalid price");
        Subscription.createSubscription(body)
                .then()
                .spec(ResponseSpec.request_failed())
                .body("error.message", containsString("No such price"));

        logger.info("✅ Correctly rejected subscription creation for nonexistent price");
    }

    @Test(groups = { "subscription", "negative", "regression" })
    public void TC_17_negative_Update_Nonexistent_Subscription() {
        logger.info("Testing update nonexistent subscription");
        Map<String, Object> body = new HashMap<>();
        body.put("metadata[key]", "value");

        logger.info("Attempting to update nonexistent subscription");
        Subscription.updateSubscription("sub_nonexistent_123", body)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.type", equalTo("invalid_request_error"));

        logger.info("✅ Correctly rejected subscription update for nonexistent subscription");
    }

    @Test(groups = { "subscription", "negative", "regression" })
    public void TC_18_negative_Cancel_Already_Canceled_Subscription() {
        logger.info("Testing cancel already canceled subscription");
        String cancelledSubscriptionId = TestContext.getCancelledSubscriptionId();
        if (cancelledSubscriptionId == null) {
            String subId = SubscriptionHelper.createFullSubscription();
            logger.info("Created subscription ID: {}", subId);
            subscriptionIdsToCleanup.add(subId);

            // First cancel succeeds
            logger.info("Attempting first cancellation of subscription ID: {}", subId);
            Subscription.cancelSubscription(subId)
                    .then()
                    .spec(ResponseSpec.OK())
                    .body("status", equalTo("canceled"));
            logger.info("First cancellation succeeded");
            cancelledSubscriptionId = subId;
            TestContext.setCancelledSubscriptionId(subId);

        }

        // Second cancel should either return already canceled or fail. Stripe API
        // allows cancelling an already canceled subscription (returns it with canceled
        // status), so let's verify it remains canceled.
        logger.info("Attempting second cancellation of subscription ID: {}", cancelledSubscriptionId);
        Subscription.cancelSubscription(cancelledSubscriptionId)
                .then()
                .spec(ResponseSpec.OK())
                .body("status", equalTo("canceled"));

        logger.info("✅ Verified redundant cancel on already canceled subscription");
    }

    // ═══════════════════════════════════════════════════════════════
    // ██ E2E FLOW TESTS
    // ═══════════════════════════════════════════════════════════════

    @Test(groups = { "subscription", "regression", "subscription_e2e", "e2e" })
    public void TC_19_flow_E2E_Subscription_Lifecycle() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));
        logger.info("🔄 Starting E2E Subscription Lifecycle Flow");

        // 1️⃣ Create customer
        String name = CustomersHelper.getName();
        String email = faker.internet().safeEmailAddress();
        Response custResp = Customer.createCustomer(name, email, null);
        String customerId = custResp.jsonPath().getString("id");
        customerIdsToCleanup.add(customerId);
        logger.info("  Step 1: Customer created → {}", customerId);

        // 2️⃣ Create valid payment method
        String paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(false);
        logger.info("  Step 2: Payment method created → {}", paymentMethodId);

        // 3️⃣ Attach payment method to customer
        Map<String, Object> attachBody = new HashMap<>();
        attachBody.put("customer", customerId);
        paymentMethods.attachPaymentMethod(paymentMethodId, attachBody)
                .then()
                .spec(ResponseSpec.OK());
        logger.info("  Step 3: Attached payment method to customer");

        // 4️⃣ Update customer default payment method for invoices
        Customer.updateCustomer(customerId,
                "invoice_settings[default_payment_method]", paymentMethodId, null)
                .then()
                .spec(ResponseSpec.OK())
                .body("invoice_settings.default_payment_method", equalTo(paymentMethodId));
        logger.info("  Step 4: Default payment method set for invoices");

        // 5️⃣ Create Product
        Map<String, Object> prodBody = new HashMap<>();
        String prodName = "E2E Premium Suite - " + System.currentTimeMillis();
        prodBody.put("name", prodName);
        prodBody.put("type", "service");
        String productId = Product.createProduct(prodBody)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("id");
        productIdsToCleanup.add(productId);
        logger.info("  Step 5: Product created → {}", productId);

        // 6️⃣ Create Recurring Price
        Map<String, Object> priceBody = new HashMap<>();
        priceBody.put("product", productId);
        priceBody.put("unit_amount", 9900); // $99.00
        priceBody.put("currency", "usd");
        priceBody.put("recurring[interval]", "month");
        String priceId = Price.createPrice(priceBody)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("id");
        logger.info("  Step 6: Price created → {}", priceId);

        // 7️⃣ Create Subscription
        Map<String, Object> subBody = new HashMap<>();
        subBody.put("customer", customerId);
        subBody.put("items[0][price]", priceId);
        String subId = Subscription.createSubscription(subBody)
                .then()
                .spec(ResponseSpec.OK())
                .body("status", equalTo("active"))
                .body("customer", equalTo(customerId))
                .body("items.data[0].price.id", equalTo(priceId))
                .extract()
                .jsonPath()
                .getString("id");
        subscriptionIdsToCleanup.add(subId);
        logger.info("  Step 7: Subscription created & active → {}", subId);

        // 8️⃣ Retrieve Subscription to verify details
        Subscription.retrieveSubscription(subId)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(subId))
                .body("status", equalTo("active"))
                .body("latest_invoice", notNullValue());
        logger.info("  Step 8: Subscription retrieval verified ✅");

        // 9️⃣ Cancel Subscription
        Subscription.cancelSubscription(subId)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(subId))
                .body("status", equalTo("canceled"));
        logger.info("  Step 9: Subscription canceled successfully ✅");

        logger.info("🎉 E2E Subscription Lifecycle Flow complete!");
    }

    // ═══════════════════════════════════════════════════════════════
    // ██ CLEANUP
    // ═══════════════════════════════════════════════════════════════

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        logger.info("Running cleanup for SubscriptionTests");
        boolean isFlow = Arrays.asList(currentGroups).contains("e2e");
        if (isFlow) {
            logger.info("In flow mode, bypassing SubscriptionTests cleanup (delegated to suite cleanup)");
            return; // Skip cleanup in flow runs to allow dependent classes/suite to clean up or
                    // proceed
        }

        // 1️⃣ Cancel all tracked subscriptions (must happen before customer deletion)
        logger.info("Canceling {} subscriptions", subscriptionIdsToCleanup.size());
        for (String subId : subscriptionIdsToCleanup) {
            try {
                logger.info("Canceling Subscription ID: {}", subId);
                Subscription.cancelSubscription(subId);
            } catch (Exception e) {
                logger.warn("⚠️ Could not cancel subscription (may already be canceled): {}", subId);
            }
        }
        subscriptionIdsToCleanup.clear();

        // 2️⃣ Delete all locally tracked customers (e.g., E2E flow customers)
        logger.info("Deleting {} locally tracked customers", customerIdsToCleanup.size());
        for (String custId : customerIdsToCleanup) {
            try {
                logger.info("Deleting Customer ID: {}", custId);
                Customer.deleteCustomer(custId);
            } catch (Exception e) {
                logger.error("⚠️ Cleanup failed for customer: {}", custId, e);
            }
        }
        customerIdsToCleanup.clear();

        // 3️⃣ Delete all locally tracked products
        logger.info("Deleting {} products", productIdsToCleanup.size());
        for (String prodId : productIdsToCleanup) {
            try {
                logger.info("Deleting Product ID: {}", prodId);
                Product.deleteProduct(prodId);
            } catch (Exception e) {
                logger.warn("⚠️ Could not delete product: {}", prodId);
            }
        }
        productIdsToCleanup.clear();

        // NOTE: TestContext values (subscriptionId, productId, priceId, customerId,
        // etc.)
        // are intentionally NOT cleared here. Shared context must remain intact for
        // any downstream test class that runs after this one in the same suite.
        logger.info("✅ SubscriptionTests cleanup complete.");
    }
}

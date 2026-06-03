package tests;

import dataprovider.PaymentMethodsDataProvider;
import endpoints.paymentMethods;
import helpers.NegativeTestHelper;
import helpers.PaymentMethodsHelper;
import helpers.TestContext;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.Test;
import specification.ResponseSpec;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import testbase.BaseClass;

public class PaymentMethodTests extends BaseClass {

    @Test(groups = { "payment_method", "positive", "flow", "regression" }, dataProvider = "createPaymentMethod", dataProviderClass = PaymentMethodsDataProvider.class)
    public void TC_01_Create_Valid_Payment_Method(String type, Map<String, Object> method) {
        logger.info("Testing create valid payment method of type: {}", type);
        String email = TestContext.getBillingEmail();
        String name = TestContext.getBillingName();
        if (email == null) {
            name = faker.name().fullName();
            email = name.replaceAll(" ", "") + "@test.com";
            TestContext.setBillingEmail(email);
            TestContext.setBillingName(name);
            logger.info("Set billing email and name in context: {} <--> {}", email, name);
        }

        method.put("billing_details[email]", email);
        method.put("billing_details[name]", name);

        logger.info("Creating payment method");
        paymentMethods.createPaymentMethod(method)
                .then()
                .spec(ResponseSpec.OK())
                .body("type", equalTo(type));
        logger.info("Successfully created valid payment method");
    }

    @Test(groups = { "payment_method", "negative", "regression" }, dataProvider = "createInvalidPaymentMethod", dataProviderClass = PaymentMethodsDataProvider.class)
    public void TC_02_Negative_PaymentMethod(String type, Map<String, Object> method) {
        logger.info("Testing negative create payment method of type: {}", type);
        paymentMethods.createPaymentMethod(method)
                .then()
                .spec(ResponseSpec.bad_request())
                .body("error.type", equalTo("invalid_request_error"));
        logger.info("Successfully verified negative create payment method rejection");
    }

    @Test(groups = { "flow", "payment_method", "positive", "smoke", "regression" }, dependsOnMethods = "tests.CustomerTests.TC_01_CreateCustomer_ValidData", ignoreMissingDependencies = true)
    public void TC_03_Attach_Payment_Method() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));
        logger.info("Testing attach payment method");

        String customerId = TestContext.getCustomerId();
        logger.info("Customer ID fetched from context: \t" + customerId);
        if (customerId == null) {
            helpers.NegativeTestHelper.createCustomerNegativeTestCase();
            customerId = TestContext.getCustomerId();
            logger.info("New customerId created --> \t" + customerId);
        }
        Map<String, Object> body = new HashMap<>();
        body.put("customer", customerId);
        String paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod();
        TestContext.setPaymentMethodId(paymentMethodId);
        logger.info("PaymentMethodId set in Context \t" + paymentMethodId);

        logger.info("Attaching payment method: {} to customer: {}", paymentMethodId, customerId);
        paymentMethods.attachPaymentMethod(paymentMethodId, body)
                .then()
                .spec(ResponseSpec.OK())
                .body("customer", equalTo(customerId));
        logger.info("Successfully attached payment method to customer");
    }

    @Test(groups = { "payment_method", "negative", "regression" }, dataProvider = "attachPaymentMethodNegative", dataProviderClass = PaymentMethodsDataProvider.class)
    public void TC_04_Attach_Invalid_Payment_Method(String customerId, String paymentMethodId,
            ResponseSpecification spec, String messsage) {
        logger.info("Testing attach invalid payment method: customerId={}, paymentMethodId={}", customerId, paymentMethodId);

        if (customerId == null) {
            NegativeTestHelper.createCustomerNegativeTestCase();
            customerId = TestContext.getCustomerId();
            logger.info("Created fallback customer ID: {}", customerId);
        }
        if (paymentMethodId == null) {
            paymentMethodId = PaymentMethodsHelper.createInvalidPaymentMethod();
            logger.info("Created fallback invalid payment method: {}", paymentMethodId);
        }
        Map<String, Object> body = new HashMap<>();
        body.put("customer", customerId);

        logger.info("Attempting to attach payment method");
        paymentMethods.attachPaymentMethod(paymentMethodId, body)
                .then()
                .spec(spec)
                .body("error.code",
                        anyOf(containsString("resource_missing"), containsString("parameter_invalid_empty")));
        logger.info("Successfully verified attach invalid payment method rejection");
    }

    @Test(groups = { "payment_method", "positive", "regression" }, dependsOnMethods = "TC_01_Create_Valid_Payment_Method", ignoreMissingDependencies = true)
    public void TC_05_Retrieve_Payment_Method() {
        logger.info("Testing retrieve valid payment method");
        String paymentMethodId = TestContext.getPaymentMethodId();
        if (paymentMethodId == null) {
            paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(false);
            logger.info("Created fallback payment method ID: {}", paymentMethodId);
        } else {
            logger.info("Using active payment method ID: {}", paymentMethodId);
        }

        logger.info("Retrieving payment method ID: {}", paymentMethodId);
        paymentMethods.retrievePaymentMethod(paymentMethodId)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(paymentMethodId));
        logger.info("Successfully retrieved payment method");
    }

    @Test(groups = { "payment_method", "negative", "regression" })
    public void TC_06_Retrieve_Invalid_Payment_Method() {
        logger.info("Testing retrieve invalid payment method");
        String paymentMethodId = "***";

        paymentMethods.retrievePaymentMethod(paymentMethodId)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.code", equalTo("resource_missing"))
                .body("error.message", containsString("No such PaymentMethod"));
        logger.info("Successfully verified retrieve invalid payment method rejection");
    }

    @Test(groups = { "payment_method", "positive", "regression" }, dependsOnMethods = "TC_01_Create_Valid_Payment_Method", ignoreMissingDependencies = true)
    public void TC_07_Retrieve_Valid_Payment_Method_By_Customer() {
        logger.info("Testing retrieve valid payment method by customer");
        String paymentMethodId = TestContext.getPaymentMethodId();
        String customerId = TestContext.getCustomerId();
        if (customerId == null) {
            helpers.NegativeTestHelper.createCustomerNegativeTestCase();
            customerId = TestContext.getCustomerId();
            logger.info("Created fallback customer ID: {}", customerId);
        }
        if (paymentMethodId == null) {
            paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(false);
            logger.info("Created fallback payment method ID: {}", paymentMethodId);
        }

        logger.info("Retrieving payment method: {} by customer: {}", paymentMethodId, customerId);
        paymentMethods.retrievePaymentMethodByCustomer(customerId, paymentMethodId)
                .then()
                .spec(ResponseSpec.OK())
                .body("customer", equalTo(customerId))
                .body("id", equalTo(paymentMethodId))
                .body("object", equalTo("payment_method"));
        logger.info("Successfully retrieved payment method by customer");
    }

    @Test(groups = { "payment_method", "negative", "regression" }, dataProvider = "retrieveInvalidPaymentMethodByCustomer", dataProviderClass = PaymentMethodsDataProvider.class)
    public void TC_08_Retrieve_InValid_Payment_Method_By_Customer(String customerId, String paymentMethodId,
            ResponseSpecification resp) {
        logger.info("Testing retrieve invalid payment method by customer: customerId={}, paymentMethodId={}", customerId, paymentMethodId);

        if (customerId == null) {
            NegativeTestHelper.createCustomerNegativeTestCase();
            customerId = TestContext.getCustomerId();
            logger.info("Created fallback customer ID: {}", customerId);
        }

        if (paymentMethodId == null) {
            paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod();
            logger.info("Created fallback payment method ID: {}", paymentMethodId);
        }

        paymentMethods.retrievePaymentMethodByCustomer(customerId, paymentMethodId)
                .then()
                .spec(resp);
        logger.info("Successfully verified retrieve invalid payment method by customer rejection");
    }

    @Test(groups = { "payment_method", "positive", "regression" }, dependsOnMethods = "TC_03_Attach_Payment_Method", ignoreMissingDependencies = true)
    public void TC_09_Detach_Payment_Method() {
        logger.info("Testing detach payment method");
        String customerId = TestContext.getCustomerId();
        if (customerId == null) {
            helpers.NegativeTestHelper.createCustomerNegativeTestCase();
            customerId = TestContext.getCustomerId();
            logger.info("Created fallback customer ID: {}", customerId);
        }

        // Create a temporary payment method to detach so that the primary one remains
        // usable for full flow
        String tempPaymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(false);
        logger.info("Created temporary payment method ID: {}", tempPaymentMethodId);

        Map<String, Object> attachBody = new HashMap<>();
        attachBody.put("customer", customerId);
        logger.info("Attaching temporary payment method to customer");
        paymentMethods.attachPaymentMethod(tempPaymentMethodId, attachBody);

        // Detach the newly created payment method
        logger.info("Detaching temporary payment method ID: {}", tempPaymentMethodId);
        paymentMethods.detachPaymentMethod(tempPaymentMethodId)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(tempPaymentMethodId))
                .body("customer", nullValue());
        logger.info("Successfully detached payment method");
    }

    @Test(groups = { "payment_method", "negative", "regression" }, dataProvider = "detachPaymentMethodNegativeCases", dataProviderClass = PaymentMethodsDataProvider.class)
    public void TC_10_negative_Detach_Payment_Method(String paymentMethodId, ResponseSpecification spec,
            String errorMessage) {
        logger.info("Testing negative detach payment method: {}", paymentMethodId);
        String customerId = TestContext.getCustomerId();
        if (customerId == null) {
            helpers.NegativeTestHelper.createCustomerNegativeTestCase();
            customerId = TestContext.getCustomerId();
            logger.info("Created fallback customer ID: {}", customerId);
        }

        // If the case requires a real payment method that was attached -> detached
        if ("SETUP_ALREADY_DETACHED".equals(paymentMethodId)) {
            paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(false);
            logger.info("Setup: Created valid temporary payment method ID: {}", paymentMethodId);
            Map<String, Object> attachBody = new HashMap<>();
            attachBody.put("customer", customerId);
            logger.info("Setup: Attaching temporary payment method");
            paymentMethods.attachPaymentMethod(paymentMethodId, attachBody);
            // Detach it once successfully
            logger.info("Setup: Performing first detach");
            paymentMethods.detachPaymentMethod(paymentMethodId);
        }

        // Try detaching (will hit the negative scenario)
        logger.info("Attempting detach payment method: {}", paymentMethodId);
        paymentMethods.detachPaymentMethod(paymentMethodId)
                .then()
                .spec(spec)
                .body("error.message", containsString(errorMessage));
        logger.info("Successfully verified negative detach rejection: {}", errorMessage);
    }
}
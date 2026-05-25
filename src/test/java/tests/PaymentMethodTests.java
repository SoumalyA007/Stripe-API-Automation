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

    @Test(groups = {
            "flow" }, dataProvider = "createPaymentMethod", dataProviderClass = PaymentMethodsDataProvider.class)
    public void TC_01_Create_Valid_Payment_Method(String type, Map<String, Object> method) {

        String email = TestContext.getBillingEmail();
        String name = TestContext.getBillingName();
        if (email == null) {
            name = faker.name().fullName();
            email = name.replaceAll(" ", "") + "@test.com";
            TestContext.setBillingEmail(email);
            TestContext.setBillingName(name);
        }

        method.put("billing_details[email]", email);
        method.put("billing_details[name]", name);
        paymentMethods.createPaymentMethod(method)
                .then()
                .spec(ResponseSpec.OK())
                .body("type", equalTo(type));

    }

    @Test(groups = {
            "unit" }, dataProvider = "createInvalidPaymentMethod", dataProviderClass = PaymentMethodsDataProvider.class)
    public void TC_02_Negative_PaymentMethod(String type, Map<String, Object> method) {

        paymentMethods.createPaymentMethod(method)
                .then()
                .spec(ResponseSpec.bad_request())
                .body("error.type", equalTo("invalid_request_error"));

    }

    @Test(groups = { "flow", "unit" }, dependsOnMethods = "tests.CustomerTests.TC_01_CreateCustomer_ValidData")
    public void TC_03_Attach_Payment_Method() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

        String customerId = TestContext.getCustomerId();
        logger.info("Customer ID fetched from context: \t" + customerId);
        if (customerId == null) {
            helpers.NegativeTestHelper.createCustomerNegativeTestCase();
            logger.info("New customerId created --> \t" + customerId);
            customerId = TestContext.getCustomerId();
        }
        Map<String, Object> body = new HashMap<>();
        body.put("customer", customerId);
        String paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod();
        TestContext.setPaymentMethodId(paymentMethodId);
        logger.info("PaymentMethodId set in Context \t" + paymentMethodId);
        paymentMethods.attachPaymentMethod(paymentMethodId, body)
                .then()
                .spec(ResponseSpec.OK())
                .body("customer", equalTo(customerId));

    }

    @Test(groups = {
            "unit" }, dataProvider = "attachPaymentMethodNegative", dataProviderClass = PaymentMethodsDataProvider.class)
    public void TC_04_Attach_Invalid_Payment_Method(String customerId, String paymentMethodId,
            ResponseSpecification spec, String messsage) {

        if (customerId == null) {
            NegativeTestHelper.createCustomerNegativeTestCase();
            customerId = TestContext.getCustomerId();
        }
        if (paymentMethodId == null) {
            paymentMethodId = PaymentMethodsHelper.createInvalidPaymentMethod();
        }
        Map<String, Object> body = new HashMap<>();
        body.put("customer", customerId);
        paymentMethods.attachPaymentMethod(paymentMethodId, body)
                .then()
                .spec(spec)
                .body("error.code",
                        anyOf(containsString("resource_missing"), containsString("parameter_invalid_empty")));

    }

    @Test(groups = { "unit" }, dependsOnMethods = "TC_01_Create_Valid_Payment_Method")
    public void TC_05_Retrieve_Payment_Method() {

        String paymentMethodId = TestContext.getPaymentMethodId();
        if (paymentMethodId == null) {
            paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(false);
        }
        paymentMethods.retrievePaymentMethod(paymentMethodId)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(paymentMethodId));
    }

    @Test(groups = { "unit" })
    public void TC_06_Retrieve_Invalid_Payment_Method() {

        String paymentMethodId = "***";
        paymentMethods.retrievePaymentMethod(paymentMethodId)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.code", equalTo("resource_missing"))
                .body("error.message", containsString("No such PaymentMethod"));
    }

    @Test(groups = { "unit" }, dependsOnMethods = "TC_01_Create_Valid_Payment_Method")
    public void TC_07_Retrieve_Valid_Payment_Method_By_Customer() {

        String paymentMethodId = TestContext.getPaymentMethodId();
        String customerId = TestContext.getCustomerId();
        if (customerId == null) {
            helpers.NegativeTestHelper.createCustomerNegativeTestCase();
            customerId = TestContext.getCustomerId();
        }
        if (paymentMethodId == null) {
            paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(false);
        }
        paymentMethods.retrievePaymentMethodByCustomer(customerId, paymentMethodId)
                .then()
                .spec(ResponseSpec.OK())
                .body("customer", equalTo(customerId))
                .body("id", equalTo(paymentMethodId))
                .body("object", equalTo("payment_method"));
    }

    @Test(groups = {
            "unit" }, dataProvider = "retrieveInvalidPaymentMethodByCustomer", dataProviderClass = PaymentMethodsDataProvider.class)
    public void TC_08_Retrieve_InValid_Payment_Method_By_Customer(String customerId, String paymentMethodId,
            ResponseSpecification resp) {

        if (customerId == null) {
            NegativeTestHelper.createCustomerNegativeTestCase();
            customerId = TestContext.getCustomerId();
        }

        if (paymentMethodId == null) {
            paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod();
        }

        paymentMethods.retrievePaymentMethodByCustomer(customerId, paymentMethodId)
                .then()
                .spec(resp);
    }

    @Test(groups = { "unit" }, dependsOnMethods = "TC_03_Attach_Payment_Method")
    public void TC_09_Detach_Payment_Method() {

        String customerId = TestContext.getCustomerId();
        if (customerId == null) {
            helpers.NegativeTestHelper.createCustomerNegativeTestCase();
            customerId = TestContext.getCustomerId();
        }

        // Create a temporary payment method to detach so that the primary one remains
        // usable for full flow
        String tempPaymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(false);

        Map<String, Object> attachBody = new HashMap<>();
        attachBody.put("customer", customerId);
        paymentMethods.attachPaymentMethod(tempPaymentMethodId, attachBody);

        // Detach the newly created payment method
        paymentMethods.detachPaymentMethod(tempPaymentMethodId)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(tempPaymentMethodId))
                .body("customer", nullValue());
    }

    @Test(groups = {
            "unit" }, dataProvider = "detachPaymentMethodNegativeCases", dataProviderClass = PaymentMethodsDataProvider.class)
    public void TC_10_negative_Detach_Payment_Method(String paymentMethodId, ResponseSpecification spec,
            String errorMessage) {

        String customerId = TestContext.getCustomerId();
        if (customerId == null) {
            helpers.NegativeTestHelper.createCustomerNegativeTestCase();
            customerId = TestContext.getCustomerId();
        }

        // If the case requires a real payment method that was attached -> detached
        if ("SETUP_ALREADY_DETACHED".equals(paymentMethodId)) {
            paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(false);
            Map<String, Object> attachBody = new HashMap<>();
            attachBody.put("customer", customerId);
            paymentMethods.attachPaymentMethod(paymentMethodId, attachBody);
            // Detach it once successfully
            paymentMethods.detachPaymentMethod(paymentMethodId);
        }

        // Try detaching (will hit the negative scenario)
        paymentMethods.detachPaymentMethod(paymentMethodId)
                .then()
                .spec(spec)
                .body("error.message", containsString(errorMessage));
    }

}
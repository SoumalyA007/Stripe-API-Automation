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

import java.util.HashMap;
import java.util.Map;

public class PaymentMethodTests {


    @Test(dataProvider = "createPaymentMethod",dataProviderClass = PaymentMethodsDataProvider.class)
    public void TC_01_Create_Valid_Payment_Method(String type, Map<String,Object> method){

        method.put("billing_details[email]", TestContext.getBillingEmail());
        method.put("billing_details[name]",TestContext.getBillingName());
        paymentMethods.createPaymentMethod(method)
                .then()
                .spec(ResponseSpec.OK())
                .body("type",equalTo(type));

    }

    @Test(dataProvider = "createInvalidPaymentMethod",dataProviderClass = PaymentMethodsDataProvider.class)
    public void TC_02_Negative_PaymentMethod(String type, Map<String,Object> method){

        paymentMethods.createPaymentMethod(method)
                .then()
                .spec(ResponseSpec.bad_request())
                .body("error.type", equalTo("invalid_request_error"));

    }

    @Test(groups = "requiresCustomer")
    public void TC_03_Attach_Payment_Method(){

        String customerId = TestContext.getCustomerId();
        Map<String,Object> body = new HashMap<>();
        body.put("customer",customerId);
        String paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod();
        TestContext.setPaymentMethodId(paymentMethodId);
        paymentMethods.attachPaymentMethod(paymentMethodId,body)
                .then()
                .spec(ResponseSpec.OK())
                .body("customer",equalTo(customerId));

    }

    @Test(dataProvider = "attachPaymentMethodNegative",dataProviderClass = PaymentMethodsDataProvider.class)
    public void TC_04_Attach_Invalid_Payment_Method(String customerId, String paymentMethodId, ResponseSpecification spec, String messsage){

        if(customerId == null){
            NegativeTestHelper.createCustomerNegativeTestCase();
            customerId = TestContext.getCustomerId();
        }
        if(paymentMethodId == null){
            paymentMethodId = PaymentMethodsHelper.createInvalidPaymentMethod();
        }
        Map<String,Object> body = new HashMap<>();
        body.put("customer",customerId);
        paymentMethods.attachPaymentMethod(paymentMethodId,body)
                .then()
                .spec(spec)
                .body("error.code",anyOf(containsString("resource_missing")
                        ,containsString("parameter_invalid_empty")));

    }


    @Test(dependsOnMethods ="TC_01_Create_Valid_Payment_Method")
    public void TC_05_Retrieve_Payment_Method(){

        String paymentMethodId = TestContext.getPaymentMethodId();
        paymentMethods.retrievePaymentMethod(paymentMethodId)
                .then()
                .spec(ResponseSpec.OK())
                .body("id",equalTo(paymentMethodId));
    }

    @Test
    public void TC_06_Retrieve_Invalid_Payment_Method(){

        String paymentMethodId = "***";
        paymentMethods.retrievePaymentMethod(paymentMethodId)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.code",equalTo("resource_missing"))
                .body("error.message",containsString("No such PaymentMethod"));
    }

    @Test(dependsOnMethods ="TC_01_Create_Valid_Payment_Method")
    public void TC_07_Retrieve_Valid_Payment_Method_By_Customer(){

        String paymentMethodId = TestContext.getPaymentMethodId();
        String customerId = TestContext.getCustomerId();
        paymentMethods.retrievePaymentMethodByCustomer(customerId,paymentMethodId)
                .then()
                .spec(ResponseSpec.OK())
                .body("customer",equalTo(customerId))
                .body("id",equalTo(paymentMethodId))
                .body("object",equalTo("payment_method"));
    }

    @Test(dataProvider = "retrieveInvalidPaymentMethodByCustomer", dataProviderClass = PaymentMethodsDataProvider.class)
    public void TC_08_Retrieve_InValid_Payment_Method_By_Customer(String customerId, String paymentMethodId,ResponseSpecification resp){

        if(customerId ==  null){
            NegativeTestHelper.createCustomerNegativeTestCase();
            customerId = TestContext.getCustomerId();
        }

        if(paymentMethodId == null){
            paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod();
        }

        paymentMethods.retrievePaymentMethodByCustomer(customerId,paymentMethodId)
                .then()
                .spec(resp);
    }

    @Test(groups = "requiresCustomer", dependsOnMethods = "TC_03_Attach_Payment_Method")
    public void TC_09_Detach_Payment_Method(){

        String paymentMethodId = TestContext.getPaymentMethodId();
        paymentMethods.detachPaymentMethod(paymentMethodId)
                .then()
                .spec(ResponseSpec.OK())
                .body("id",equalTo(paymentMethodId))
                .body("customer",nullValue());
    }





}
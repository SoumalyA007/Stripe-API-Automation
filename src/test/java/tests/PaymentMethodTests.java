package tests;

import dataprovider.PaymentMethodsDataProvider;
import endpoints.paymentMethods;
import helpers.PaymentMethodsHelper;
import helpers.TestContext;
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
        paymentMethods.attachPaymentMethod(PaymentMethodsHelper.createValidPaymentMethod(),body)
                .then()
                .spec(ResponseSpec.OK())
                .body("customer",equalTo(customerId));

    }

    @Test(groups = "requiresCustomer")
    public void TC_04_Attach_Invalid_Payment_Method(){

        String customerId = TestContext.getCustomerId();
        Map<String,Object> body = new HashMap<>();
        body.put("customer",customerId);
        paymentMethods.attachPaymentMethod(PaymentMethodsHelper.createInvalidPaymentMethod(),body)
                .then()
                .spec(ResponseSpec.OK())
                .body("customer",equalTo(customerId));

    }

    @Test
    public void TC_05_Attach_Invalid_Customer_ID(String customerId, ){

        String customerId = "test";
        Map<String,Object> body = new HashMap<>();
        body.put("customer",customerId);
        paymentMethods.attachPaymentMethod(PaymentMethodsHelper.createInvalidPaymentMethod(),body)
                .then()
                .spec(ResponseSpec.OK())
                .body("customer",equalTo(customerId));

    }

    @Test
    public void TC_05_Attach_Invalid_Customer_ID(){

    }




}

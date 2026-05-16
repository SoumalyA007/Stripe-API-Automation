package tests;

import dataprovider.PaymentMethodsDataProvider;
import endpoints.paymentMethods;
import helpers.TestContext;
import org.testng.annotations.Test;
import specification.ResponseSpec;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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

    @Test
    public void TC_03_Attach_Payment_Method(){

    }




}

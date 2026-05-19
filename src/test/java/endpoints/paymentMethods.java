package endpoints;
import io.restassured.response.Response;
import specification.RequestSpec;

import java.util.Map;

import static io.restassured.RestAssured.given;


public class paymentMethods {



    public static Response createPaymentMethod(Map<String, Object> body){

        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/payment_methods")
                .formParams(body)
                .when()
                .post();

    }

    public static Response attachPaymentMethod(String paymentMethodId,Map<String, Object> body){
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/payment_methods/{id}/attach")
                .pathParam("id",paymentMethodId)
                .formParams(body)
                .when()
                .post();
    }

    public static Response retrievePaymentMethod(String paymentMethodId){
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/payment_methods/{id}")
                .pathParam("id",paymentMethodId)
                .when()
                .get();
    }


    public static Response retrievePaymentMethodByCustomer(String customerId,String paymentMethodId){
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/customers/{customerId}/payment_methods/{paymentMethodId}")
                .pathParam("customerId",customerId)
                .pathParam("paymentMethodId",paymentMethodId)
                .when()
                .get();
    }


}
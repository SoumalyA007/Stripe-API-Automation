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

    public static Response attachPaymentMethod(String id,){
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/payment_methods/{id}/attach")
                .pathParam("id",id)
                .formParams(body)
                .when()
                .post();
    }


}

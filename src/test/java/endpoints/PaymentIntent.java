package endpoints;

import io.restassured.response.Response;
import specification.RequestSpec;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static testbase.BaseClass.p;

public class PaymentIntent {

    public static Response createPaymentIntent(Map<String, Object> body) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/payment_intents")
                .formParams(body)
                .when()
                .post();
    }

}

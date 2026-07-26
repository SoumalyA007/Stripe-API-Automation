package endpoints;

import io.restassured.response.Response;
import specification.RequestSpec;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class PaymentIntent {

    public static Response createPaymentIntent(Map<String, Object> body) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/payment_intents")
                .formParams(body)
                .when()
                .post();
    }

    public static Response cancelPaymentIntent(String paymentIntentId, Map<String, Object> body) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/payment_intents/{id}/cancel")
                .pathParam("id", paymentIntentId)
                .formParams(body)
                .when()
                .post();
    }

    public static Response confirmPaymentIntent(String paymentIntentId, Map<String, Object> body) {
        return confirmPaymentIntent(paymentIntentId, body, new HashMap<>());
    }

    public static Response confirmPaymentIntent(String paymentIntentId, Map<String, Object> body,
            Map<String, String> headers) {
        return given()
                .spec(RequestSpec.setupv1())
                .headers(headers)
                .basePath("/v1/payment_intents/{id}/confirm")
                .pathParam("id", paymentIntentId)
                .formParams(body)
                .when()
                .post();
    }

    public static Response retrievePaymentIntent(String paymentIntentId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/payment_intents/{id}")
                .pathParam("id", paymentIntentId)
                .when()
                .get();
    }

    public static Response retrieveCharge(String chargeId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/charges/{id}")
                .pathParam("id", chargeId)
                .when()
                .get();
    }
}

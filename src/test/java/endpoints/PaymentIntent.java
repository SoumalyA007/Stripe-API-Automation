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

    /**
     * Retrieves a PaymentIntent by its ID.
     *
     * @param paymentIntentId the PaymentIntent ID (pi_xxx)
     * @return the full PaymentIntent response
     */
    public static Response retrievePaymentIntent(String paymentIntentId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/payment_intents/{id}")
                .pathParam("id", paymentIntentId)
                .when()
                .get();
    }

    /**
     * Retrieves a Charge by its ID using the Stripe /v1/charges endpoint.
     *
     * @param chargeId the Charge ID (ch_xxx)
     * @return the full Charge response
     */
    public static Response retrieveCharge(String chargeId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/charges/{id}")
                .pathParam("id", chargeId)
                .when()
                .get();
    }
}

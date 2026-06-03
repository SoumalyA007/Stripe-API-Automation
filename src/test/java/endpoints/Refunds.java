package endpoints;

import static io.restassured.RestAssured.given;
import static testbase.BaseClass.p;

import java.util.HashMap;
import java.util.Map;

import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import specification.RequestSpec;

public class Refunds {

    // ============== CREATE ==============

    public static Response createRefund(String paymentIntentId, Map<String, Object> body) {
        return createRefund(paymentIntentId, body, new HashMap<>());
    }

    public static Response createRefund(String paymentIntentId, Map<String, Object> body, Map<String, String> headers) {
        return given()
                .spec(RequestSpec.setupv1())
                .headers(headers)
                .basePath("/v1/refunds")
                .formParams(body)
                .when()
                .post();
    }

    public static Response createRefundWithCustomAuth(String token, Map<String, Object> body) {

        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v1/refunds")
                .contentType("application/x-www-form-urlencoded");

        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }

        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .formParams(body)
                .when()
                .post();
    }


    // ============== RETRIEVE ==============

    public static Response retrieveRefund(String refundId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/refunds/{refundId}")
                .pathParam("refundId", refundId)
                .when()
                .get();
    }

    public static Response retrieveRefundWithCustomAuth(String token, String refundId) {

        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v1/refunds/{refundId}")
                .pathParam("refundId", refundId)
                .contentType("application/x-www-form-urlencoded");

        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }

        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .get();
    }


    // ============== CANCEL ==============

    public static Response cancelRefund(String refundId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/refunds/{refundId}/cancel")
                .pathParam("refundId", refundId)
                .when()
                .post();
    }

    public static Response cancelRefundWithCustomAuth(String token, String refundId) {

        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v1/refunds/{refundId}/cancel")
                .pathParam("refundId", refundId)
                .contentType("application/x-www-form-urlencoded");

        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }

        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .post();
    }

}

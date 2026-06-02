package endpoints;

import static io.restassured.RestAssured.given;
import static testbase.BaseClass.p;

import java.util.Map;

import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import specification.RequestSpec;

public class Payouts {

    // ============== CREATE ==============

    public static Response createPayout(Map<String, Object> body) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/payouts")
                .formParams(body)
                .when()
                .post();
    }

    public static Response createPayoutWithCustomAuth(String token, Map<String, Object> body) {
        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v1/payouts")
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

    public static Response retrievePayout(String payoutId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/payouts/{payoutId}")
                .pathParam("payoutId", payoutId)
                .when()
                .get();
    }

    public static Response retrievePayoutWithCustomAuth(String token, String payoutId) {
        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v1/payouts/{payoutId}")
                .pathParam("payoutId", payoutId)
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

    public static Response cancelPayout(String payoutId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/payouts/{payoutId}/cancel")
                .pathParam("payoutId", payoutId)
                .when()
                .post();
    }

    public static Response cancelPayoutWithCustomAuth(String token, String payoutId) {
        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v1/payouts/{payoutId}/cancel")
                .pathParam("payoutId", payoutId)
                .contentType("application/x-www-form-urlencoded");

        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }

        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .post();
    }

    // ============== LIST ==============

    public static Response listPayouts(Map<String, Object> queryParams) {
        var request = given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/payouts");
        if (queryParams != null && !queryParams.isEmpty()) {
            request.queryParams(queryParams);
        }
        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .get();
    }

    public static Response listPayoutsWithCustomToken(String token, Map<String, Object> queryParams) {
        var request = given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/payouts");
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        if (queryParams != null && !queryParams.isEmpty()) {
            request.queryParams(queryParams);
        }
        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .get();
    }
}

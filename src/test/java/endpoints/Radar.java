package endpoints;

import static io.restassured.RestAssured.given;
import static testbase.BaseClass.p;

import java.util.Map;

import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import specification.RequestSpec;

public class Radar {

    // ============== RETRIEVE ==============

    public static Response retrieveEarlyFraudWarning(String warningId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/radar/early_fraud_warnings/{warningId}")
                .pathParam("warningId", warningId)
                .when()
                .get();
    }

    public static Response retrieveEarlyFraudWarningWithCustomAuth(String token, String warningId) {
        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v1/radar/early_fraud_warnings/{warningId}")
                .pathParam("warningId", warningId)
                .contentType("application/x-www-form-urlencoded");

        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }

        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .get();
    }

    // ============== LIST ==============

    public static Response listEarlyFraudWarnings(Map<String, Object> queryParams) {
        var request = given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/radar/early_fraud_warnings");
        if (queryParams != null && !queryParams.isEmpty()) {
            request.queryParams(queryParams);
        }
        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .get();
    }

    public static Response listEarlyFraudWarningsWithCustomToken(String token, Map<String, Object> queryParams) {
        var request = given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/radar/early_fraud_warnings");
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

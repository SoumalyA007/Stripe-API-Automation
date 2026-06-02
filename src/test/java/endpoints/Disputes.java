package endpoints;

import static io.restassured.RestAssured.given;
import static testbase.BaseClass.p;

import java.util.Map;

import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import specification.RequestSpec;

public class Disputes {

    // ============== RETRIEVE ==============

    public static Response retrieveDispute(String disputeId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/disputes/{disputeId}")
                .pathParam("disputeId", disputeId)
                .when()
                .get();
    }

    public static Response retrieveDisputeWithCustomAuth(String token, String disputeId) {
        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v1/disputes/{disputeId}")
                .pathParam("disputeId", disputeId)
                .contentType("application/x-www-form-urlencoded");

        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }

        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .get();
    }

    // ============== UPDATE ==============

    public static Response updateDispute(String disputeId, Map<String, Object> body) {
        var request = given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/disputes/{disputeId}")
                .pathParam("disputeId", disputeId);

        if (body != null && !body.isEmpty()) {
            body.forEach((key, value) -> {
                if (value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nested = (Map<String, Object>) value;
                    nested.forEach((nk, nv) -> request.formParam(key + "[" + nk + "]", nv));
                } else {
                    request.formParam(key, value);
                }
            });
        }

        return request.when().post();
    }

    public static Response updateDisputeWithCustomAuth(String token, String disputeId, Map<String, Object> body) {
        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v1/disputes/{disputeId}")
                .pathParam("disputeId", disputeId)
                .contentType("application/x-www-form-urlencoded");

        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }

        if (body != null && !body.isEmpty()) {
            body.forEach((key, value) -> {
                if (value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nested = (Map<String, Object>) value;
                    nested.forEach((nk, nv) -> request.formParam(key + "[" + nk + "]", nv));
                } else {
                    request.formParam(key, value);
                }
            });
        }

        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .post();
    }

    // ============== CLOSE ==============

    public static Response closeDispute(String disputeId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/disputes/{disputeId}/close")
                .pathParam("disputeId", disputeId)
                .when()
                .post();
    }

    public static Response closeDisputeWithCustomAuth(String token, String disputeId) {
        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v1/disputes/{disputeId}/close")
                .pathParam("disputeId", disputeId)
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

    public static Response listDisputes(Map<String, Object> queryParams) {
        var request = given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/disputes");
        if (queryParams != null && !queryParams.isEmpty()) {
            request.queryParams(queryParams);
        }
        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .get();
    }

    public static Response listDisputesWithCustomToken(String token, Map<String, Object> queryParams) {
        var request = given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/disputes");
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

package endpoints;

import io.restassured.response.Response;
import specification.RequestSpec;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class SetupIntent {

    public static Response createSetupIntent(Map<String, Object> body) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/setup_intents")
                .formParams(body)
                .when()
                .post();
    }

    public static Response confirmSetupIntent(String setupIntentId, Map<String, Object> body) {
        return confirmSetupIntent(setupIntentId, body, new HashMap<>());
    }

    public static Response confirmSetupIntent(String setupIntentId, Map<String, Object> body,
            Map<String, String> headers) {
        return given()
                .spec(RequestSpec.setupv1())
                .headers(headers)
                .basePath("/v1/setup_intents/{id}/confirm")
                .pathParam("id", setupIntentId)
                .formParams(body)
                .when()
                .post();
    }

    public static Response retrieveSetupIntent(String setupIntentId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/setup_intents/{id}")
                .pathParam("id", setupIntentId)
                .when()
                .get();
    }

    public static Response updateSetupIntent(String setupIntentId, Map<String, Object> body) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/setup_intents/{id}")
                .pathParam("id", setupIntentId)
                .formParams(body)
                .when()
                .post();
    }

    public static Response cancelSetupIntent(String setupIntentId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/setup_intents/{id}/cancel")
                .pathParam("id", setupIntentId)
                .when()
                .post();
    }

    public static Response cancelSetupIntent(String setupIntentId, Map<String, Object> body) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/setup_intents/{id}/cancel")
                .pathParam("id", setupIntentId)
                .formParams(body)
                .when()
                .post();
    }

    public static Response listSetupIntents(Map<String, Object> queryParams) {
        var request = given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/setup_intents");
        if (queryParams != null && !queryParams.isEmpty()) {
            request.queryParams(queryParams);
        }
        return request.when().get();
    }
}

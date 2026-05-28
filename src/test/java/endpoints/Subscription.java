package endpoints;

import io.restassured.response.Response;
import specification.RequestSpec;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class Subscription {

    // =============== SUBSCRIPTION ENDPOINTS ===============

    public static Response createSubscription(Map<String, Object> body) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/subscriptions")
                .formParams(body)
                .when()
                .post();
    }

    public static Response retrieveSubscription(String subscriptionId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/subscriptions/{id}")
                .pathParam("id", subscriptionId)
                .when()
                .get();
    }

    public static Response updateSubscription(String subscriptionId, Map<String, Object> body) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/subscriptions/{id}")
                .pathParam("id", subscriptionId)
                .formParams(body)
                .when()
                .post();
    }

    public static Response cancelSubscription(String subscriptionId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/subscriptions/{id}")
                .pathParam("id", subscriptionId)
                .when()
                .delete();
    }

    public static Response listSubscriptions(Map<String, Object> queryParams) {
        var request = given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/subscriptions");
        if (queryParams != null && !queryParams.isEmpty()) {
            request.queryParams(queryParams);
        }
        return request.when().get();
    }
}

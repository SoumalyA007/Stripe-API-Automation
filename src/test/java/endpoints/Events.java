package endpoints;

import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import specification.RequestSpec;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static testbase.BaseClass.p;

public class Events {

    public static Response getEvents(Map<String, Object> queryParams) {
        var request = given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/events");
        if (queryParams != null && !queryParams.isEmpty()) {
            request.queryParams(queryParams);
        }
        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .get();
    }

    public static Response getEventsWithCustomAuth(String token, Map<String, Object> queryParams) {
        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v1/events");
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

    public static Response getEvent(String id) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/events/{id}")
                .pathParam("id", id)
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .get();
    }

    public static Response getEventWithCustomAuth(String token, String id) {
        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v1/events/{id}")
                .pathParam("id", id);
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .get();
    }
}

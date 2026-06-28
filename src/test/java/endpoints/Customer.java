package endpoints;

import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import specification.RequestSpec;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static testbase.BaseClass.p;

public class Customer {

    public static Response createCustomer(Object name, Object email, Map<String, String> metadata) {
        return createCustomer(name, email, metadata, new HashMap<>());
    }

    public static Response createCustomer(Object name, Object email, Map<String, String> metadata,
            Map<String, String> headers) {

        var request = given()
                .spec(RequestSpec.setupv1())
                .headers(headers)
                .basePath("/v1/customers");

        if (name != null) {
            request.formParam("name", name);
        }
        if (email != null) {
            request.formParam("email", email);
        }
        if (metadata != null) {
            metadata.forEach((key, value) -> request.formParam("metadata[" + key + "]", value));
        }
        return request.when().post();

    }

    public static Response createCustomerWithCustomAuth(String token, String name, String email) {
        var request = given()
                .baseUri(p.getProperty("baseURI"));
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        if (name != null) {
            request.formParams("name", name);
        }
        if (email != null) {
            request.formParams("email", email);
        }

        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .post("/v1/customers");

    }

    public static Response updateCustomer(String id, String fieldName, String fieldValue,
            Map<String, String> metadata) {

        var request = given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/customers/{id}")
                .pathParam("id", id);

        if (fieldName != null && !fieldName.equals("metadata") && fieldValue != null) {
            request.formParams(fieldName, fieldValue);
        }
        if (fieldName.equals("metadata") && !metadata.isEmpty()) {
            metadata.forEach((key, value) -> request.formParam("metadata[" + key + "]", value));
        }
        return request.when().post();

    }

    public static Response updateCustomerWithCustomAuth(String token, String id, String fieldName, String fieldValue,
            Map<String, String> metadata) {
        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v1/customers/{id}")
                .pathParam("id", id);
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        if (fieldName != null && !fieldName.equals("metadata") && fieldValue != null) {
            request.formParams(fieldName, fieldValue);
        }
        if (fieldName.equals("metadata") && !metadata.isEmpty()) {
            metadata.forEach((key, value) -> request.formParam("metadata[" + key + "]", value));
        }

        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .post();

    }

    public static Response getCustomer(String id) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/customers/{id}")
                .pathParam("id", id)
                .filter(new RequestLoggingFilter())
                .filter(new ResponseLoggingFilter())
                .when()
                .get();

    }

    public static Response getCustomerWithCustomAuth(String token, String id) {
        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v1/customers/{id}")
                .pathParam("id", id);
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }

        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .get();

    }

    public static Response deleteCustomer(String id) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/customers/{id}")
                .pathParam("id", id)
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .delete();
    }

    public static Response deleteCustomerWithCustomAuth(String token, String id) {
        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v1/customers/{id}")
                .pathParam("id", id);
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .delete();
    }

    public static Response listCustomers(Map<String, Object> queryParams) {

        var request = given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/customers");
        if (queryParams != null && !queryParams.isEmpty()) {
            request.queryParams(queryParams);
        }
        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .get();

    }

    public static Response listCustomersWithCustomToken(String token, Map<String, Object> queryParams) {

        var request = given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/customers");
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

    public static Response searchCustomer(String query) {

        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/customers/search")
                .queryParam("query", query)
                .when()
                .get();

    }

    public static Response searchCustomerWithCustomToken(String token, String query) {

        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v1/customers/search")
                .queryParam("query", query);
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .get();

    }

    public static Response fundCashBalance(String customerId, Map<String, Object> body) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/test_helpers/customers/{id}/fund_cash_balance")
                .pathParam("id", customerId)
                .formParams(body)
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .post();
    }

}

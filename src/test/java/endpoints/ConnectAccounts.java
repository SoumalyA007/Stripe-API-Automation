package endpoints;

import java.util.HashMap;
import java.util.Map;

import io.restassured.response.Response;
import specification.RequestSpec;
import static io.restassured.RestAssured.given;

public class ConnectAccounts {

    // Create Connect Account

    public static Response createConnectAccount(Map<String, Object> body) {

        return createConnectAccount(body, new HashMap<>());
    }

    public static Response createConnectAccount(Map<String, Object> body, Map<String, String> headers) {

        Response response = given().spec(RequestSpec.setupv1())
                .headers(headers)
                .basePath("/v1/accounts")
                .formParams(body)
                .when()
                .post();

        return response;

    }

    // Update Connect Account

    public static Response updateConnectAccount(String accountId, Map<String, Object> body) {

        return updateConnectAccount(accountId, body, new HashMap<>());
    }

    public static Response updateConnectAccount(String accountId, Map<String, Object> body,
            Map<String, String> headers) {

        Response response = given().spec(RequestSpec.setupv1())
                .headers(headers)
                .basePath("/v1/accounts/{id}")
                .pathParam("id", accountId)
                .formParams(body)
                .when()
                .post();

        return response;

    }

    // Delete Connect Account

    public static Response deleteConnectAccount(String accountId) {

        return deleteConnectAccount(accountId, new HashMap<>());
    }

    public static Response deleteConnectAccount(String accountId, Map<String, String> headers) {

        Response response = given().spec(RequestSpec.setupv1())
                .headers(headers)
                .basePath("/v1/accounts/{id}")
                .pathParam("id", accountId)
                .when()
                .delete();

        return response;

    }

    // Retrieve Connect Account

    public static Response retrieveConnectAccount(String accountId) {

        return retrieveConnectAccount(accountId, new HashMap<>());
    }

    public static Response retrieveConnectAccount(String accountId, Map<String, String> headers) {

        Response response = given().spec(RequestSpec.setupv1())
                .headers(headers)
                .basePath("/v1/accounts/{id}")
                .pathParam("id", accountId)
                .when()
                .get();

        return response;

    }

    // List Connect Accounts

    public static Response listConnectAccounts() {

        return listConnectAccounts(new HashMap<>());
    }

    public static Response listConnectAccounts(Map<String, String> headers) {

        Response response = given().spec(RequestSpec.setupv1())
                .headers(headers)
                .basePath("/v1/accounts")
                .when()
                .get();

        return response;

    }

    // Link Account

    public static Response linkAccount(Map<String, String> body) {

        return linkAccount(body, new HashMap<>());
    }

    public static Response linkAccount(Map<String, String> body, Map<String, String> headers) {

        Response response = given().spec(RequestSpec.setupv1())
                .headers(headers)
                .basePath("/v1/account_links")
                .formParams(body)
                .when()
                .post();

        return response;

    }

}

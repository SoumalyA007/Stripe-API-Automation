package endpoints;

import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import models.common.CloseAccountRequestPayload;
import models.common.CreateAccountRequestPayload;
import specification.RequestSpec;

import static io.restassured.RestAssured.given;
import static testbase.BaseClass.p;

public class accounts {

    // ============== CREATE ==============

    public static Response createAccount(CreateAccountRequestPayload body) {

        return given()
                .spec(RequestSpec.setupv2())
                .basePath("/v2/core/accounts")
                .body(body)
                .when()
                .post();

    }

    public static Response createAccountWithCustomAuth(String token, CreateAccountRequestPayload body) {

        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v2/core/accounts")
                .header("Stripe-Version", "2026-04-08.preview")
                .contentType("application/json");

        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }

        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .body(body)
                .when()
                .post();

    }

    // ============== RETRIEVE ==============

    public static Response retrieveAccount(String id) {

        return given()
                .spec(RequestSpec.setupv2())
                .basePath("/v2/core/accounts/{id}")
                .pathParam("id", id)
                .when()
                .get();

    }

    public static Response retrieveAccountWithCustomAuth(String token, String id) {

        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v2/core/accounts/{id}")
                .pathParam("id", id)
                .header("Stripe-Version", "2026-04-08.preview")
                .contentType("application/json");

        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }

        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .get();

    }

    // ============== CLOSE ==============

    public static Response closeAccount(String id, CloseAccountRequestPayload body) {

        return given()
                .spec(RequestSpec.setupv2())
                .basePath("/v2/core/accounts/{id}/close")
                .pathParam("id", id)
                .body(body)
                .when()
                .post();

    }

    /** Convenience overload — sends an empty body (only for accounts with no applied configs). */
    public static Response closeAccount(String id) {

        return given()
                .spec(RequestSpec.setupv2())
                .basePath("/v2/core/accounts/{id}/close")
                .pathParam("id", id)
                .body("{}")
                .when()
                .post();

    }

    public static Response closeAccountWithCustomAuth(String token, String id, CloseAccountRequestPayload body) {

        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v2/core/accounts/{id}/close")
                .pathParam("id", id)
                .header("Stripe-Version", "2026-04-08.preview")
                .contentType("application/json");

        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }

        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .body(body)
                .when()
                .post();

    }

    /** Convenience overload with empty body — for auth-failure tests where configs don't matter. */
    public static Response closeAccountWithCustomAuth(String token, String id) {

        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v2/core/accounts/{id}/close")
                .pathParam("id", id)
                .header("Stripe-Version", "2026-04-08.preview")
                .contentType("application/json");

        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }

        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .body("{}")
                .when()
                .post();

    }

}

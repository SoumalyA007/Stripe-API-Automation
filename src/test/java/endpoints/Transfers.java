package endpoints;

import static io.restassured.RestAssured.given;
import static testbase.BaseClass.p;

import java.util.Map;

import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import specification.RequestSpec;

public class Transfers {

    // ============== CREATE ==============

    public static Response createTransfer(Map<String, Object> body) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/transfers")
                .formParams(body)
                .when()
                .post();
    }

    public static Response createTransferWithCustomAuth(String token, Map<String, Object> body) {
        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v1/transfers")
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

    public static Response retrieveTransfer(String transferId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/transfers/{transferId}")
                .pathParam("transferId", transferId)
                .when()
                .get();
    }

    public static Response retrieveTransferWithCustomAuth(String token, String transferId) {
        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v1/transfers/{transferId}")
                .pathParam("transferId", transferId)
                .contentType("application/x-www-form-urlencoded");

        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }

        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .get();
    }

    // ============== REVERSE ==============

    public static Response reverseTransfer(String transferId, Map<String, Object> body) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/transfers/{transferId}/reversals")
                .pathParam("transferId", transferId)
                .formParams(body)
                .when()
                .post();
    }

    public static Response reverseTransferWithCustomAuth(String token, String transferId, Map<String, Object> body) {
        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v1/transfers/{transferId}/reversals")
                .pathParam("transferId", transferId)
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
}

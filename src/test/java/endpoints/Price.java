package endpoints;

import io.restassured.response.Response;
import specification.RequestSpec;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class Price {

    public static Response createPrice(Map<String, Object> body) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/prices")
                .formParams(body)
                .when()
                .post();
    }

    public static Response retrievePrice(String priceId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/prices/{id}")
                .pathParam("id", priceId)
                .when()
                .get();
    }

    public static Response listPrices(Map<String, Object> queryParams) {
        return given()
                .spec(RequestSpec.setupv1())
                .queryParams(queryParams)
                .when()
                .get("/v1/prices");
    }
}

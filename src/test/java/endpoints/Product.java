package endpoints;

import io.restassured.response.Response;
import specification.RequestSpec;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class Product {

    public static Response createProduct(Map<String, Object> body) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/products")
                .formParams(body)
                .when()
                .post();
    }

    public static Response retrieveProduct(String productId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/products/{id}")
                .pathParam("id", productId)
                .when()
                .get();
    }

    public static Response deleteProduct(String productId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/products/{id}")
                .pathParam("id", productId)
                .when()
                .delete();
    }
}

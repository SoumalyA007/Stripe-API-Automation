package endpoints;

import io.restassured.response.Response;
import builders.requestbuilder.CreateAccountRequestPayload;
import specification.RequestSpec;


import static io.restassured.RestAssured.given;

public class accounts {


    public static Response createAccount(CreateAccountRequestPayload body){

        return given()
                .spec(RequestSpec.setupv2())
                .basePath("/v2/core/accounts")
                .body(body)
                .when()
                .post();


    }




}

package endpoints;

import io.restassured.response.Response;
import specification.RequestSpec;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class Customer {

    public static Response createCustomer(Object name , Object email, Map<String, String> metadata){

        var request = given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/customers");

        if(name!=null){
            request.formParam("name" , name);
        }
        if(email!=null){
            request.formParam("email",email);
        }
        if(metadata!=null){
            metadata.forEach((key, value) ->
                    request.formParam("metadata[" + key + "]", value)
            );
        }
        return request.when().post();

    }


    public static Response deleteCustomer(String id){
        return given()
                .basePath("/v1/customers/{id}")
                .pathParam("id",id)
                .when()
                .delete();
    }

}

package endpoints;

import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import specification.RequestSpec;
import testbase.BaseClass;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static testbase.BaseClass.p;

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


    public static Response createCustomerWithCustomAuth(String token, String name, String email) {
        var request = given()
                .baseUri(p.getProperty("baseURI"));
        if (token != null) {
            request.header("Authorization","Bearer "+ token);
        }
        if (name != null) {
            request.formParams("name",name);
        }
        if (email != null) {
            request.formParams("email",email);
        }


        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .post("/v1/customers");

    }

    public static Response updateCustomer(String id, String fieldName , String fieldValue, Map<String, String> metadata){

        var request = given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/customers/{id}")
                .pathParam("id",id);

        if(fieldName != null && !fieldName.equals("metadata") && fieldValue != null){
            request.formParams(fieldName ,fieldValue);
        }
        if(fieldName.equals("metadata") && !metadata.isEmpty()){
            metadata.forEach((key, value) ->
                    request.formParam("metadata[" + key + "]", value)
            );
        }
        return request.when().post();

    }

    public static Response updateCustomerWithCustomAuth(String token, String id,  String fieldName , String fieldValue, Map<String, String> metadata) {
        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v1/customers/{id}")
                .pathParam("id",id);
        if (token != null) {
            request.header("Authorization","Bearer "+ token);
        }
        if(fieldName != null && !fieldName.equals("metadata") && fieldValue != null){
            request.formParams(fieldName ,fieldValue);
        }
        if(fieldName.equals("metadata") && !metadata.isEmpty()){
            metadata.forEach((key, value) ->
                    request.formParam("metadata[" + key + "]", value)
            );
        }


        return request
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when()
                .post();

    }





    public static Response deleteCustomer(String id){
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/customers/{id}")
                .pathParam("id",id)
                .when()
                .delete();
    }



}

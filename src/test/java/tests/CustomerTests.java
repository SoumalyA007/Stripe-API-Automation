package tests;

import com.github.javafaker.Faker;
import dataprovider.UpdateCustomerDataProvider;
import endpoints.Customer;
import helpers.Customers;
import helpers.TestContext;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import specification.RequestSpec;
import specification.ResponseSpec;
import testbase.BaseClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CustomerTests extends BaseClass {

    Faker faker = new Faker();
    List<String> customerIds = new ArrayList<>();


    //***************CREATE CUSTOMER*******************\\

    //Create a Valid Customer
    @Test
    public void createCustomerTC_01(){


        String name = Customers.getName();
        String email = faker.internet().safeEmailAddress();

        Response resp = Customer.createCustomer(name,email,null);
        String id = resp.then().spec(ResponseSpec.OK())
                .body("id", notNullValue())
                .body("email",equalTo(email))
                .body("name",equalTo(name))
                .extract()
                .jsonPath()
                .get("id");

        customerIds.add(id);

    }

    //Creating a customer with no name
    @Test
    public void createCustomerWithOnlyEmailTC_02(){

        String email = faker.internet().safeEmailAddress();
        Response resp = Customer.createCustomer(null,email,null);
        String id = resp.then().spec(ResponseSpec.OK())
                .body("id", notNullValue())
                .body("email",equalTo(email))
                .body("name",equalTo(null))
                .extract()
                .jsonPath()
                .get("id");

        customerIds.add(id);

    }


    @Test
    public void createCustomerUsingMetadata(){

        String email = faker.internet().emailAddress();
        String name = Customers.getName();
        Map<String,String> metadata = new HashMap<>();
        metadata.put("name",name);
        metadata.put("source","automation");

        Response response = Customer.createCustomer(null,email,metadata);
        String id = response.then()
                .spec(ResponseSpec.OK())
                .body("email",equalTo(email))
                .body("metadata.name",equalTo(name))
                .body("metadata.source", equalTo("automation"))
                .extract()
                .jsonPath()
                .get("id");

        customerIds.add(id);

    }

    //Create Customer with invalid token
    @Test
    public void createCustomerWithInvalidKey(){

        Response response = Customer.createCustomerWithCustomAuth("invalid", "ABC" , "ABC");
        response.then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message",containsString("Invalid API Key provided"));
    }

    //Create Customer with no token
    @Test
    public void createCustomerWithNoToken(){

        Response response = Customer.createCustomerWithCustomAuth(null, "ABC" , "ABC");
        response.then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message",containsString("You did not provide an API key"));
    }

    //create customer with invalid email format
    @Test
    public void createCustomerWithInvalidEmail(){

        String name = "test";
        String email = "abc-def";
        Response response = Customer.createCustomer(name,email,null);
        if(response.statusCode()==200){
            String id = response.then().extract().jsonPath().get("id");
            Customer.deleteCustomer(id);
        }
        response.then().spec(ResponseSpec.bad_request());


    }

    //
    @Test
    public void createCustomerwithDuplicateEmails(){

        String name = Customers.getName();
        String email = faker.internet().safeEmailAddress();

        Response firstResponse = Customer.createCustomer(name,email,null);
        String firstCustomerId = firstResponse.then().spec(ResponseSpec.OK())
                .body("id", notNullValue())
                .body("email",equalTo(email))
                .body("name",equalTo(name))
                .extract()
                .jsonPath()
                .get("id");
        Response secondResponse = Customer.createCustomer(name,email,null);
        String secondCustomerId = secondResponse.then().spec(ResponseSpec.OK())
                .body("id", notNullValue())
                .body("email",equalTo(email))
                .body("name",equalTo(name))
                .extract()
                .jsonPath()
                .get("id");

        // 🔹 🔥 IMPORTANT ASSERTIONS
        assertThat(firstCustomerId, notNullValue());
        assertThat(secondCustomerId, notNullValue());

        // ✅ Core validation: IDs must be different
        assertThat(firstCustomerId, not(equalTo(secondCustomerId)));

        // ✅ Email should be same
        assertThat(
                firstResponse.jsonPath().getString("email"),
                equalTo(secondResponse.jsonPath().getString("email"))
        );

        // 🔹 Cleanup BOTH customers
        customerIds.add(firstCustomerId);
        customerIds.add(secondCustomerId);

    }

    //create a customer with very large name
    @Test
    public void createCustomerWithVeryLongName(){

        String name = "a".repeat(550);
        String email = faker.internet().safeEmailAddress();
        Response response = Customer.createCustomer(name,email,null);
        response.then().spec(ResponseSpec.bad_request());

    }

    //create a customer name with special characters
    @Test
    public void createCustomerWithSpecialCharacters(){

        String name = "*/*/*/*#$!#!AA!!!";
        String email = faker.internet().safeEmailAddress();

        Response response = Customer.createCustomer(name, email, null);

        // 🔹 Cleanup FIRST (if accidentally created)
        if (response.getStatusCode() == 200) {
            String id = response.jsonPath().getString("id");
            customerIds.add(id); // handled by @AfterMethod
        }

        // 🔹 Main Assertion (your actual test goal)
        response.then().spec(ResponseSpec.bad_request());
    }

    @Test
    public void createCustomerWithNullValues(){

        String name = null;
        String email = null;

        Response response = Customer.createCustomer(name, email, null);

        // 🔹 Cleanup FIRST (if accidentally created)
        if (response.getStatusCode() == 200) {
            String id = response.jsonPath().getString("id");
            customerIds.add(id); // handled by @AfterMethod
        }

        // 🔹 Main Assertion (your actual test goal)
        response.then().spec(ResponseSpec.bad_request());
    }


//***************UPDATE CUSTOMER*******************\\

    //Update customer with name , email and metadata
    @Test(dataProvider ="updateDataProvider",dataProviderClass = UpdateCustomerDataProvider.class)
    public void updateCustomerWithValidData(String fieldName , String fieldValue,Map<String, String> metadata){

        Response resp =  null;
        String customerId = TestContext.getCustomerId();
        if(metadata!=null){

             resp = Customer.updateCustomer(customerId,fieldName,null,metadata);
        }else{
             resp = Customer.updateCustomer(customerId,fieldName,fieldValue,null);
        }

        resp.then().spec(ResponseSpec.OK());


    }

    //Update customer with invalid customer id
    @Test
    public void updateName(){

        Response resp =  null;
        String invalidId = "inavlid_customer_id";

        resp = Customer.updateCustomer(invalidId,"name","Invalid Test",null);

        int statusCode = resp.getStatusCode();
        if(statusCode==200){
            String id = resp.jsonPath().getString("id");
            customerIds.add(id);
        }

        resp.then().spec(ResponseSpec.not_found());


    }

    //Update customer with invalid auth
    @Test
    public void updateFieldWithWrongToken(){

        Response resp =  null;
        String customerId = TestContext.getCustomerId();

        resp = Customer.updateCustomerWithCustomAuth("invlid_token",customerId,"name","Soumalya",null);

        int statusCode = resp.getStatusCode();
        if(statusCode==200){
            String id = resp.jsonPath().getString("id");
            customerIds.add(id);
        }

        resp.then().spec(ResponseSpec.forbidden());


    }

//****************CLEANUP AFTER TEST*****************\\
    @AfterMethod
    public void cleanup() {

        for (String id : customerIds) {
            try {
                Customer.deleteCustomer(id);
            } catch (Exception e) {
                System.out.println("Cleanup failed for customer: " + id);
            }
        }

        // 🔥 Important: clear list after cleanup
        customerIds.clear();
    }









}


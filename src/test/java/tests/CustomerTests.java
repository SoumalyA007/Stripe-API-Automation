package tests;

import com.github.javafaker.Faker;
import dataprovider.UpdateCustomerDataProvider;
import endpoints.Customer;
import helpers.Customers;
import helpers.TestContext;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import specification.ResponseSpec;
import testbase.BaseClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CustomerTests extends BaseClass {

    Faker faker = new Faker();
    List<String> customerIds = new ArrayList<>();


    //***************CREATE CUSTOMER*******************\\

    //Create a Valid Customer
    @Test(groups = {"customer.create", "positive", "smoke", "regression"})
    public void TC_01_CreateCustomer_ValidData(){


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
    @Test(groups = {"customer.create", "positive", "regression"})
    public void TC_02_CreateCustomer_OnlyEmail(){

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

    //Create Customer with MetaaData
    @Test(groups = {"customer.create", "positive", "regression"})
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
    @Test(groups = {"customer.create", "negative", "auth", "regression"})
    public void TC_04_CreateCustomer_InvalidApiKey(){

        Response response = Customer.createCustomerWithCustomAuth("invalid", "ABC" , "ABC");
        response.then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message",containsString("Invalid API Key provided"));
    }

    //create customer with invalid email format
    @Test(groups = {"customer.create", "negative", "validation", "edge", "regression"})
    public void TC_05_CreateCustomer_InvalidEmailFormat(){

        String name = "test";
        String email = "abc-def";
        Response response = Customer.createCustomer(name,email,null);
        if(response.statusCode()==200){
            String id = response.then().extract().jsonPath().get("id");
            Customer.deleteCustomer(id);
        }
        response.then().spec(ResponseSpec.bad_request());


    }

    //Create Customer with no token
    @Test(groups = {"customer.create", "negative", "auth", "regression"})
    public void TC_06_CreateCustomer_MissingAuth(){

        Response response = Customer.createCustomerWithCustomAuth(null, "ABC" , "ABC");
        response.then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message",containsString("You did not provide an API key"));
    }

    //Cretae Customer with duplicate mail
    @Test(groups = {"customer.create", "edge", "regression"})
    public void TC_07_CreateCustomer_DuplicateEmail(){

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
    @Test(groups = {"customer.create", "edge", "validation", "regression"})
    public void TC_08_CreateCustomer_LongName(){

        String name = "a".repeat(550);
        String email = faker.internet().safeEmailAddress();
        Response response = Customer.createCustomer(name,email,null);
        response.then().spec(ResponseSpec.bad_request());

    }

    //create a customer name with special characters
    @Test(groups = {"customer.create", "edge", "validation", "regression"})
    public void TC_09_CreateCustomer_SpecialCharacters(){

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

    //create a customer with values set as null
    @Test(groups = {"customer.create", "edge", "validation", "regression"})
    public void TC_10_CreateCustomer_EmptyValues(){

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
    @Test(groups = {"customer.update", "positive", "smoke", "regression", "requiresCustomer"},dataProvider ="updateDataProvider",dataProviderClass = UpdateCustomerDataProvider.class)
    public void TC_01_UpdateCustomer_Name(String fieldName , String fieldValue,Map<String, String> metadata){

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
    @Test(groups = {"customer.update", "negative", "validation", "regression"})
    public void TC_02_UpdateCustomer_InvalidId(){

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
    @Test(groups = {"customer.update", "negative", "auth", "regression"})
    public void TC_03_UpdateCustomer_InvalidAuth(){

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

    //Update customer with missing auth
    @Test(groups = {"customer.update", "negative", "auth", "regression"})
    public void TC_04_UpdateCustomer_MissingAuth(){

        Response resp =  null;
        String customerId = TestContext.getCustomerId();

        resp = Customer.updateCustomerWithCustomAuth(null,customerId,"name","Soumalya",null);

        int statusCode = resp.getStatusCode();
        if(statusCode==200){
            String id = resp.jsonPath().getString("id");
            customerIds.add(id);
        }

        resp.then().spec(ResponseSpec.Unauthorized());

    }

    //Update deleted Customer
    @Test(groups = {"customer.update", "edge", "destructive", "regression", "requiresCustomer"})
    public void TC_05_UpdateCustomer_DeletedCustomer(){

        Response resp;
        String customerId = TestContext.getCustomerId();
        Customers.deleteCustomer(customerId);
        resp = Customer.updateCustomer(customerId,"name","Invalid Test",null);

        int statusCode = resp.getStatusCode();
        if(statusCode==200){
            String id = resp.jsonPath().getString("id");
            customerIds.add(id);
        }

        resp.then().spec(ResponseSpec.not_found());

    }




//****************RETRIEVE DATA TEST*****************\\

    //Get data with valid customer Id
    @Test(groups = {"customer.update", "edge", "destructive", "regression", "requiresCustomer"})
    public void TC_01_RetrieveCustomer_ValidId(){

        String customerId = TestContext.getCustomerId();
        Response resp = Customer.getCustomer(customerId);

        resp.then().spec(ResponseSpec.OK())
                .body("id",equalTo(customerId));

    }

    //Get customer with valid Id
    @Test(groups = {"customer.retrieve", "negative", "validation", "regression"})
    public void TC_02_RetrieveCustomer_InvalidId(){

        Response resp = Customer.getCustomer("invalid_id");
        resp.then().spec(ResponseSpec.not_found());

    }

    //Get Customer data with invalidId
    @Test(groups = {"customer.retrieve", "negative", "auth", "regression","requiresCustomer"})
    public void TC_03_RetrieveCustomer_MissingAuth(){
        String customerId = TestContext.getCustomerId();
        Response resp = Customer.getCustomerWithCustomAuth(null,customerId);
        resp.then().spec(ResponseSpec.Unauthorized());

    }

    //Get customer with deleted customer's id
    @Test(groups = {"customer.retrieve", "edge", "regression","requiresCustomer"})
    public void TC_04_RetrieveCustomer_DeletedCustomer(){
        String customerId = TestContext.getCustomerId();
        Response resp = Customer.getCustomer(customerId);
        resp.then().spec(ResponseSpec.not_found());
    }


//****************DELETE CUSTOMER TEST*****************\\

    //Delete valid customer
    @Test(groups = {"customer.delete", "positive", "destructive", "regression", "requiresCustomer"})
    public void TC_01_DeleteCustomer_Valid(){

        String customerId = TestContext.getCustomerId();
        Customer.deleteCustomer(customerId)
                .then()
                .spec(ResponseSpec.OK());

    }

    //Delete invalid customer
    @Test(groups = {"customer.delete", "negative", "validation", "regression"})
    public void TC_02_DeleteCustomer_InvalidId(){

        String customerId = "invalid";
        Customer.deleteCustomer(customerId)
                .then()
                .spec(ResponseSpec.not_found());

    }

    //Delete already deleted customer
    @Test(groups = {"customer.delete", "negative", "validation", "regression","requiresCustomer"})
    public void  TC_03_DeleteCustomer_AlreadyDeleted(){

        String customerId = TestContext.getCustomerId();
        Customer.deleteCustomer(customerId);
        Customer.deleteCustomer(customerId)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.code",equalTo("resource_missing"));

    }

    @Test(groups = {"customer.retrieve", "negative", "auth", "regression","requiresCustomer"})
    public void TC_04_DeleteCustomer_MissingAuth(){
        String customerId = TestContext.getCustomerId();

        Customer.deleteCustomerWithCustomAuth(null,customerId)
                .then()
                .spec(ResponseSpec.Unauthorized());
    }

//****************LIST CUSTOMER TEST*****************\\

    //default customer list
    @Test
    public void TC_01_ListCustomers_Default(){

        Map<String,Object> queryParams = new HashMap<>();

        Customer.listCustomers(queryParams)
                .then()
                .spec(ResponseSpec.OK());
    }

    //Get the list of only 2 customer
    @Test
    public void TC_02_ListCustomers_WithFilter(){

        Map<String,Object> queryParams = new HashMap<>();
        queryParams.put("limit",2);
        Customer.listCustomers(queryParams)
                .then()
                .spec(ResponseSpec.OK())
                .body("data.size()",equalTo(2));
    }

    //Get the result based on pagination
    @Test
    public void TC_03_ListCustomers_WithPagination(){
        Map<String,Object> queryParams = new HashMap<>();
        queryParams.put("starting_after","cus_UNt0BtOK1xydSU");
        Customer.listCustomers(queryParams)
                .then()
                .spec(ResponseSpec.OK())
                .body("data.id",not(hasItem("cus_UOqwXnBZ7zW9BX")));

    }

    //Get customerlist with invalid token
    @Test
    public void TC_04_ListCustomers_WithInvalidToken(){

        Map<String,Object> queryParams = new HashMap<>();

        Customer.listCustomersWithCustomToken("invalid_token",queryParams)
                .then()
                .spec(ResponseSpec.Unauthorized());
    }

//****************SEARCH CUSTOMER TEST*****************\\

    //Search a customer by Email
    @Test
    public void TC_01_SearchCustomer_ByEmail(){

        Customer.searchCustomer("email:'furever@example.com'")
                .then()
                .spec(ResponseSpec.OK())
                .body("data.email",everyItem(equalTo("furever@example.com")));

    }

    //Search a customer by nonexisting email
    @Test
    public void TC_02_SearchCustomer_ByInvalidEmail(){

        Customer.searchCustomer("email:'nullll'")
                .then()
                .spec(ResponseSpec.OK())
                .body("data.size()",equalTo(0));

    }

    //Search a customer by valid email with Invalid Query Syntax
    @Test
    public void TC_03_SearchCustomer_ByInvalidQuerySyntax(){

        Customer.searchCustomer("email->'furever@example.com'")
                .then()
                .spec(ResponseSpec.bad_request());

    }

    //Search a customer by valid email with Invalid Token
    @Test
    public void TC_04_SearchCustomer_ByInvalidToken(){

        Customer.searchCustomer("email:'furever@example.com'")
                .then()
                .spec(ResponseSpec.Unauthorized());

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

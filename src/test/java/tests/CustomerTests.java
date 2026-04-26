package tests;

import com.github.javafaker.Faker;
import endpoints.Customer;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import specification.ResponseSpec;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CustomerTests {

    Faker faker = new Faker();
    String customerId = "";

    //Create a Valid Customer
    @Test
    public void createCustomerTC_01(){


        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String name = firstName + " " + lastName;
        String email = faker.internet().safeEmailAddress();

        Response resp = Customer.createCustomer(name,email,null);
        customerId = resp.then().spec(ResponseSpec.OK())
                .body("id", notNullValue())
                .body("email",equalTo(email))
                .body("name",equalTo(name))
                .extract()
                .jsonPath()
                .get("id");

        deleteCustomer(customerId);

    }

    //Creating a customer with no name
    @Test
    public void createCustomerWithOnlyEmailTC_02(){

        String email = faker.internet().safeEmailAddress();
        Response resp = Customer.createCustomer(null,email,null);
        customerId = resp.then().spec(ResponseSpec.OK())
                .body("id", notNullValue())
                .body("email",equalTo(email))
                .body("name",equalTo(null))
                .extract()
                .jsonPath()
                .get("id");

        deleteCustomer(customerId);

    }


    @Test
    public void createCustomerUsingMetadata(){

        String email = faker.internet().emailAddress();
        String name = faker.name().firstName() + faker.name().lastName();
        Map<String,String> metadata = new HashMap<>();
        metadata.put("name",name);
        metadata.put("source","automation");

        Response response = Customer.createCustomer(null,email,metadata);
        String id = response.then()
                .spec(ResponseSpec.OK())
                .body("email",equalTo(email))
                .body("metadata.name",equalTo(metadata.get(name)))
                .body("metadata.source", equalTo("automation"))
                        .extract()
                                .jsonPath()
                                        .get("id");

        deleteCustomer(id);

    }






    @Test
    public void deleteCustomer(String customerId){
        Customer.deleteCustomer(customerId);
    }



}


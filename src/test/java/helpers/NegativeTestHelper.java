package helpers;

import com.github.javafaker.Faker;
import endpoints.Customer;
import io.restassured.response.Response;

public class NegativeTestHelper {


    public static void createCustomerNegativeTestCase(){
        Faker faker = new Faker();
        String name = faker.name().fullName();
        String email = name.replaceAll(" ","")+ "@test.com";
        Response resp = Customer.createCustomer("Regression User", email, null);

        String customerId = resp.jsonPath().getString("id");
        TestContext.setCustomerId(customerId);
        TestContext.setBillingEmail(email);
        TestContext.setBillingName(name);

        System.out.println("✅ Suite customer created: " + customerId);
    }
}
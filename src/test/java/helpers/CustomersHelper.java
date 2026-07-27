package helpers;

import com.github.javafaker.Faker;
import endpoints.Customer;
import io.restassured.response.Response;

public class CustomersHelper {

    public static Faker faker = new Faker();

    public static String getName() {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String name = firstName + " " + lastName;

        return name;
    }

    public static void deleteCustomer(String id) {
        Customer.deleteCustomer(id);
    }

    public static String createCustomer() {
        Faker faker = new Faker();
        String name = faker.name().fullName();
        String email = name.replaceAll(" ", "") + "@test.com";
        Response resp = Customer.createCustomer(name, email, null);

        String customerId = resp.jsonPath().getString("id");
        return customerId;

    }

    public static String createCustomer(String email) {
        Faker faker = new Faker();
        String name = faker.name().fullName();
        return createCustomer(name, email);
    }

    private static String createCustomer(String name, String email) {
        Response resp = Customer.createCustomer(name, email, null);
        return resp.jsonPath().getString("id");
    }

}

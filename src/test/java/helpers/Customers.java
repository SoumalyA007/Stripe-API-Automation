package helpers;


import com.github.javafaker.Faker;
import endpoints.Customer;

public class Customers {

    public static Faker faker = new Faker();

    public static String getName(){
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String name = firstName + " " + lastName;

        return name;
    }



}

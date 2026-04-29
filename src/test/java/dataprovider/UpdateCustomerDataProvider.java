package dataprovider;

import com.github.javafaker.Faker;
import org.testng.annotations.DataProvider;

import java.util.HashMap;
import java.util.Map;

public class UpdateCustomerDataProvider {

    Faker faker = new Faker();

    @DataProvider(name = "updateDataProvider")
    public Object[][] updateDataProvider(){
        Map<String,String> metadata = new HashMap<>();
        metadata.put("name",faker.name().fullName());
        metadata.put("source","automation");
        return new Object[][]{
                {"name",faker.name().fullName(),null},
                {"email",faker.internet().emailAddress(),null},
                {"metadata",null,metadata}
        };
    }


}

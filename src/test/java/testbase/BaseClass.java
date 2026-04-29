package testbase;

import java.io.FileReader;
import java.util.Properties;

import endpoints.Customer;
import helpers.TestContext;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public class BaseClass {

    public static Properties p = new Properties();
    public Logger logger = LogManager.getLogger(this.getClass());

    static{
        try{
            FileReader fr = new FileReader("src/test/resources/config.properties");
            p.load(fr);
        }catch(Exception e){
            throw new RuntimeException("❌ Failed to load config.properties", e);
        }
    }

    @BeforeSuite
    public void beforeSuiteSetup(){
        logger.info("Suite Start Now :) ");
        logger.info("Base URI: " + p.getProperty("baseURI"));

        // ✅ Create customer ONCE for the entire regression suite
        String email = "regression+" + System.currentTimeMillis() + "@test.com";
        Response resp = Customer.createCustomer("Regression User", email, null);

        String customerId = resp.jsonPath().getString("id");
        TestContext.setCustomerId(customerId);

        System.out.println("✅ Suite customer created: " + customerId);
    }


    @AfterSuite
    public void afterSuiteSetup(){
        logger.info("Suite End Now :) ");
        // 🧹 Cleanup after ALL tests are done
        String customerId = TestContext.getCustomerId();
        if (customerId != null) {
            Customer.deleteCustomer(customerId);
            System.out.println("🧹 Suite customer deleted: " + customerId);
        }


    }


}

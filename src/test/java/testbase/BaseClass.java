package testbase;

import java.io.FileReader;
import java.util.Properties;

import com.github.javafaker.Faker;
import helpers.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.testng.annotations.Test;
import org.testng.annotations.Listeners;

@Listeners(listeners.ExtentReportListener.class)
public class BaseClass {

    public static Properties p = new Properties();
    public Logger logger = LogManager.getLogger(this.getClass());
    public static Faker faker = new Faker();
    public static final int amount;

    static {
        try {
            FileReader fr = new FileReader("src/test/resources/config.properties");
            p.load(fr);
            amount = Integer.parseInt(p.getProperty("amount"));
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to load config.properties", e);
        }
    }

    @BeforeSuite
    public void beforeSuiteSetup() {
        logger.info("Suite Start Now :) ");
        logger.info("Base URI: " + p.getProperty("baseURI"));

    }

    // @BeforeMethod(onlyForGroups = "requiresCustomer")
    // public void createCustomer(){
    // // ✅ Create customer ONCE for the entire regression suite
    // String email = "regression+" + System.currentTimeMillis() + "@test.com";
    // Response resp = Customer.createCustomer("Regression User", email, null);
    //
    // String customerId = resp.jsonPath().getString("id");
    // TestContext.setCustomerId(customerId);
    //
    // System.out.println("✅ Suite customer created: " + customerId);
    // }

    public String[] currentGroups = {};

    @BeforeMethod(alwaysRun = true)
    public void captureTestGroups(Method method) {
        Test testAnnotation = method.getAnnotation(Test.class);
        currentGroups = (testAnnotation != null) ? testAnnotation.groups() : new String[] {};
        logger.info("▶ Running: [{}] | Groups: {}", method.getName(), Arrays.toString(currentGroups));
    }

    @BeforeMethod(onlyForGroups = "unit")
    public void clearContextForUnitTest() {
        TestContext.clear();
        logger.info("🧹 Cleared TestContext for isolated unit test.");
    }

    @AfterSuite()
    public void afterSuiteSetup() {
        // Resource cleanup and TestContext clearing are handled by
        // SuiteCleanupListener (ISuiteListener.onFinish), which fires after
        // every XML suite. Nothing to do here.
        logger.info("Suite End Now :) ");
    }

}
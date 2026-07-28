package testbase;

import java.io.FileNotFoundException;
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
    public static int amount;

    static {
        try {
            FileReader fr = new FileReader("src/test/resources/config.properties");
            p.load(fr);
            amount = Integer.parseInt(p.getProperty("amount", "2000"));
        } catch (FileNotFoundException e) {
            // ── CI / Jenkins path ─────────────────────────────────────────────
            // config.properties is gitignored and won't exist on CI agents.
            // Credentials are injected as environment variables instead.
            String baseURI = System.getenv("STRIPE_BASE_URI");
            String authKey = System.getenv("STRIPE_AUTH_KEY");
            String amountEnv = System.getenv("STRIPE_AMOUNT");
            String merchantId = System.getenv("STRIPE_MERCHANT_ACCOUNT_ID");

            if (authKey == null || authKey.isBlank()) {
                throw new RuntimeException(
                        "❌ config.properties not found and STRIPE_AUTH_KEY env var is not set. " +
                                "Set Jenkins credentials or create config.properties locally.");
            }

            p.setProperty("baseURI", baseURI != null ? baseURI : "https://api.stripe.com");
            p.setProperty("authKey", authKey);
            p.setProperty("amount", amountEnv != null ? amountEnv : "2000");
            p.setProperty("merchant_account_id", merchantId != null ? merchantId : "");

            amount = Integer.parseInt(p.getProperty("amount", "2000"));
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to load config: " + e.getMessage(), e);
        }
    }

    @BeforeSuite
    public void beforeSuiteSetup() {
        logger.info("Suite Start Now :) ");
        logger.info("Base URI: " + p.getProperty("baseURI"));

    }

    public String[] currentGroups = {};

    @BeforeMethod(alwaysRun = true)
    public void captureTestGroups(Method method) {
        Test testAnnotation = method.getAnnotation(Test.class);
        currentGroups = (testAnnotation != null) ? testAnnotation.groups() : new String[] {};
        logger.info("▶ Running: [{}] | Groups: {}", method.getName(), Arrays.toString(currentGroups));
        logger.info(
                "Thread={} | Class={} | Test={}",
                Thread.currentThread().getName(),
                this.getClass().getSimpleName(),
                method.getName()
        );
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
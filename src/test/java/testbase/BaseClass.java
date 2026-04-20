package testbase;

import java.io.FileReader;
import java.util.Properties;
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
    }


    @AfterSuite
    public void afterSuiteSetup(){
        logger.info("Suite End Now :) ");

    }




}

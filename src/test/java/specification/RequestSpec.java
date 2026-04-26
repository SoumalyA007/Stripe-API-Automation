package specification;


import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import testbase.BaseClass;

public class RequestSpec extends BaseClass {

    public static RequestSpecification setupv2(){

        System.out.println(p.getProperty("authKey"));
        RequestSpecification spec = new RequestSpecBuilder()
                .setBaseUri(p.getProperty("baseURI"))
                .addHeader("Authorization", "Bearer " + p.getProperty("authKey"))
                .addHeader("Stripe-Version","2026-04-08.preview")
                .setContentType("application/json")
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();

        return spec;
    }

    public static RequestSpecification setupv1(){

        System.out.println(p.getProperty("authKey"));
        RequestSpecification spec = new RequestSpecBuilder()
                .setBaseUri(p.getProperty("baseURI"))
                .addHeader("Authorization", "Bearer " + p.getProperty("authKey"))
                .setContentType("application/x-www-form-urlencoded")
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();

        return spec;
    }

}

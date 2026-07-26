package specification;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import logfilter.FailedApiLoggingFilter;
import testbase.BaseClass;

public class RequestSpec extends BaseClass {

    public static RequestSpecification setupv2() {

        RequestSpecification spec = new RequestSpecBuilder()
                .setBaseUri(p.getProperty("baseURI"))
                .addHeader("Authorization", "Bearer " + p.getProperty("authKey"))
                .addHeader("Stripe-Version", "2026-04-08.preview")
                .setContentType("application/json")
                .addFilter(new FailedApiLoggingFilter())
                .build();

        return spec;
    }

    public static RequestSpecification setupv1() {

        RequestSpecification spec = new RequestSpecBuilder()
                .setBaseUri(p.getProperty("baseURI"))
                .addHeader("Authorization", "Bearer " + p.getProperty("authKey"))
                .setContentType("application/x-www-form-urlencoded")
                .addFilter(new FailedApiLoggingFilter())
                .build();

        return spec;
    }

}

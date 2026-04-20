package tests;

import com.github.javafaker.Faker;
import datafactory.CreateAccountDataFactory;
import endpoints.accounts;
import enums.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import specification.ResponseSpec;

import java.util.List;

public class AccountTests extends ResponseSpec {

    Faker faker = new Faker();

    private static ThreadLocal<Response> createAcountResponseThread = new ThreadLocal<>();

    @Test
    public void createValidAccount(){



        String email = faker.internet().emailAddress();
        String name = faker.name().firstName();
        String country = "us";
        IdentityEntity entity = IdentityEntity.company;
        Boolean CustomerRequested = true;
        Boolean merchantRequested = true;
        DefaultsResponsibilitiesFeesCollector fees_collector = DefaultsResponsibilitiesFeesCollector.stripe;
        DefaultsResponsibilitiesLossesCollector losses_collector = DefaultsResponsibilitiesLossesCollector.stripe;
        Dashboard dashboard = Dashboard.full;
        List< String > include = List.of(
                Include.CONFIGURATION_CUSTOMER.getValue(),
                Include.CONFIGURATION_MERCHANT.getValue(),
                Include.IDENTITY.getValue(),
                Include.DEFAULTS.getValue()
        );

        Response resp = accounts.createAccount(CreateAccountDataFactory.createAccountRequestPayload(email,name,country,entity,CustomerRequested,merchantRequested,fees_collector,losses_collector,dashboard,include));
        createAcountResponseThread.set(resp);
        resp.then().spec(OK());

    }

}

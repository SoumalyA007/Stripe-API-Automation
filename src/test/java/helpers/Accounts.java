package helpers;

import builders.requestbuilder.CreateAccountRequestPayload;
import com.github.javafaker.Faker;
import datafactory.CreateAccountDataFactory;
import enums.*;

import java.util.List;

public class Accounts {

    private static final Faker faker = new Faker();
    public static CreateAccountRequestPayload validAccountCreationHelper(){
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

        return CreateAccountDataFactory.createAccountRequestPayload(email,name,country,entity,CustomerRequested,merchantRequested,fees_collector,losses_collector,dashboard,include);
    }


}

package helpers;

import builders.requestbuilder.*;
import com.github.javafaker.Faker;
import datafactory.CreateAccountDataFactory;
import enums.*;

import java.util.List;

public class AccountsHelper {

    private static final Faker faker = new Faker();

    // ============== FULL VALID ACCOUNT (all fields populated) ==============

    public static CreateAccountRequestPayload validAccountCreationHelper() {
        String email = faker.internet().emailAddress();
        String name = faker.name().firstName();
        String country = "us";
        IdentityEntity entity = IdentityEntity.company;
        Boolean CustomerRequested = true;
        Boolean merchantRequested = true;
        DefaultsResponsibilitiesFeesCollector fees_collector = DefaultsResponsibilitiesFeesCollector.stripe;
        DefaultsResponsibilitiesLossesCollector losses_collector = DefaultsResponsibilitiesLossesCollector.stripe;
        Dashboard dashboard = Dashboard.full;
        List<String> include = List.of(
                Include.CONFIGURATION_CUSTOMER.getValue(),
                Include.CONFIGURATION_MERCHANT.getValue(),
                Include.IDENTITY.getValue(),
                Include.DEFAULTS.getValue()
        );

        return CreateAccountDataFactory.createAccountRequestPayload(
                email, name, country, entity,
                CustomerRequested, merchantRequested,
                fees_collector, losses_collector,
                dashboard, include
        );
    }

    // ============== ENTITY TYPE VARIANT ==============

    public static CreateAccountRequestPayload accountWithEntityType(IdentityEntity entityType) {
        String email = faker.internet().emailAddress();
        String name = faker.name().firstName();
        return CreateAccountDataFactory.createAccountRequestPayload(
                email, name, "us", entityType,
                true, true,
                DefaultsResponsibilitiesFeesCollector.stripe,
                DefaultsResponsibilitiesLossesCollector.stripe,
                Dashboard.full,
                List.of(
                        Include.CONFIGURATION_CUSTOMER.getValue(),
                        Include.CONFIGURATION_MERCHANT.getValue(),
                        Include.IDENTITY.getValue(),
                        Include.DEFAULTS.getValue()
                )
        );
    }

    // ============== DASHBOARD TYPE VARIANT ==============

    public static CreateAccountRequestPayload accountWithDashboard(Dashboard dashboardType) {
        String email = faker.internet().emailAddress();
        String name = faker.name().firstName();
        return CreateAccountDataFactory.createAccountRequestPayload(
                email, name, "us", IdentityEntity.company,
                true, true,
                DefaultsResponsibilitiesFeesCollector.stripe,
                DefaultsResponsibilitiesLossesCollector.stripe,
                dashboardType,
                List.of(
                        Include.CONFIGURATION_CUSTOMER.getValue(),
                        Include.CONFIGURATION_MERCHANT.getValue(),
                        Include.IDENTITY.getValue(),
                        Include.DEFAULTS.getValue()
                )
        );
    }

    // ============== CUSTOMER CONFIG ONLY (no merchant) ==============

    public static CreateAccountRequestPayload accountWithCustomerConfigOnly() {
        String email = faker.internet().emailAddress();
        String name = faker.name().firstName();
        return CreateAccountRequestPayload.builder()
                .contact_email(email)
                .display_name(name)
                .identity(
                        Identity.builder()
                                .country("us")
                                .entity_type(IdentityEntity.company)
                                .business_details(
                                        Business_Details.builder()
                                                .registered_name(name)
                                                .build())
                                .build())
                .configuration(
                        Configuration.builder()
                                .customer(
                                        Customer.builder()
                                                .capabilities(
                                                        CustomerCapabilities.builder()
                                                                .automatic_indirect_tax(
                                                                        Automatic_indirect_tax.builder()
                                                                                .requested(true)
                                                                                .build())
                                                                .build())
                                                .build())
                                .build())
                .defaults(
                        Defaults.builder()
                                .responsibilities(
                                        Responsibilities.builder()
                                                .fees_collector(DefaultsResponsibilitiesFeesCollector.stripe)
                                                .losses_collector(DefaultsResponsibilitiesLossesCollector.stripe)
                                                .build())
                                .build())
                .dashboard(Dashboard.full)
                .include(List.of(
                        Include.CONFIGURATION_CUSTOMER.getValue(),
                        Include.IDENTITY.getValue(),
                        Include.DEFAULTS.getValue()))
                .build();
    }

    // ============== MERCHANT CONFIG ONLY (no customer) ==============

    public static CreateAccountRequestPayload accountWithMerchantConfigOnly() {
        String email = faker.internet().emailAddress();
        String name = faker.name().firstName();
        return CreateAccountRequestPayload.builder()
                .contact_email(email)
                .display_name(name)
                .identity(
                        Identity.builder()
                                .country("us")
                                .entity_type(IdentityEntity.company)
                                .business_details(
                                        Business_Details.builder()
                                                .registered_name(name)
                                                .build())
                                .build())
                .configuration(
                        Configuration.builder()
                                .merchant(
                                        Merchant.builder()
                                                .capabilities(
                                                        MerchantCapabilities.builder()
                                                                .card_payments(
                                                                        Card_Payments.builder()
                                                                                .requested(true)
                                                                                .build())
                                                                .build())
                                                .build())
                                .build())
                .defaults(
                        Defaults.builder()
                                .responsibilities(
                                        Responsibilities.builder()
                                                .fees_collector(DefaultsResponsibilitiesFeesCollector.stripe)
                                                .losses_collector(DefaultsResponsibilitiesLossesCollector.stripe)
                                                .build())
                                .build())
                .dashboard(Dashboard.full)
                .include(List.of(
                        Include.CONFIGURATION_MERCHANT.getValue(),
                        Include.IDENTITY.getValue(),
                        Include.DEFAULTS.getValue()))
                .build();
    }

    // ============== MINIMAL PAYLOAD (required fields only) ==============

    public static CreateAccountRequestPayload minimalAccountPayload() {
        return CreateAccountRequestPayload.builder()
                .identity(
                        Identity.builder()
                                .country("us")
                                .entity_type(IdentityEntity.individual)
                                .build())
                .build();
    }

    // ============== MISSING IDENTITY (negative) ==============

    public static CreateAccountRequestPayload accountWithoutIdentity() {
        return CreateAccountRequestPayload.builder()
                .contact_email(faker.internet().emailAddress())
                .display_name(faker.name().firstName())
                .dashboard(Dashboard.full)
                .build();
    }

    // ============== MISSING COUNTRY IN IDENTITY (negative) ==============

    public static CreateAccountRequestPayload accountWithoutCountry() {
        String name = faker.name().firstName();
        return CreateAccountRequestPayload.builder()
                .contact_email(faker.internet().emailAddress())
                .display_name(name)
                .identity(
                        Identity.builder()
                                .entity_type(IdentityEntity.company)
                                .business_details(
                                        Business_Details.builder()
                                                .registered_name(name)
                                                .build())
                                .build())
                .dashboard(Dashboard.full)
                .build();
    }

}

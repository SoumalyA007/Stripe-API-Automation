package helpers;

import java.util.List;

import com.github.javafaker.Faker;

import datafactory.CreateAccountDataFactory;
import enums.Dashboard;
import enums.DefaultsResponsibilitiesFeesCollector;
import enums.DefaultsResponsibilitiesLossesCollector;
import enums.IdentityEntity;
import enums.Include;
import models.common.Automatic_indirect_tax;
import models.common.Business_Details;
import models.common.Card_Payments;
import models.common.CloseAccountRequestPayload;
import models.common.Configuration;
import models.common.CreateAccountRequestPayload;
import models.common.Customer;
import models.common.CustomerCapabilities;
import models.common.Defaults;
import models.common.Identity;
import models.common.Merchant;
import models.common.MerchantCapabilities;
import models.common.Responsibilities;

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

    public static CreateAccountRequestPayload minimalValidAccount() {
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
            .build();
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

        DefaultsResponsibilitiesFeesCollector feesCollector =
                dashboardType == Dashboard.express
                        ? DefaultsResponsibilitiesFeesCollector.application
                        : DefaultsResponsibilitiesFeesCollector.stripe;

        DefaultsResponsibilitiesLossesCollector lossesCollector =
                dashboardType == Dashboard.express
                        ? DefaultsResponsibilitiesLossesCollector.application
                        : DefaultsResponsibilitiesLossesCollector.stripe;

        return CreateAccountDataFactory.createAccountRequestPayload(
                email, name, "us", IdentityEntity.company,
                true, true,
                feesCollector,
                lossesCollector,
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
                .include(List.of(
                        Include.CONFIGURATION_CUSTOMER.getValue(),
                        Include.IDENTITY.getValue()))
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

    // ============== CLOSE ACCOUNT PAYLOADS ==============

    /** For accounts created with both customer + merchant configurations. */
    public static CloseAccountRequestPayload closePayloadBothConfigs() {
        return CloseAccountRequestPayload.builder()
                .applied_configurations(List.of("customer", "merchant"))
                .build();
    }

    /** For accounts created with customer configuration only. */
    public static CloseAccountRequestPayload closePayloadCustomerOnly() {
        return CloseAccountRequestPayload.builder()
                .applied_configurations(List.of("customer"))
                .build();
    }

    /** For accounts created with merchant configuration only. */
    public static CloseAccountRequestPayload closePayloadMerchantOnly() {
        return CloseAccountRequestPayload.builder()
                .applied_configurations(List.of("merchant"))
                .build();
    }

}

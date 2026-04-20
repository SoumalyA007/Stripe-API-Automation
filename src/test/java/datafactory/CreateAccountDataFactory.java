package datafactory;

import builders.requestbuilder.*;
import enums.*;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateAccountDataFactory {

    public static CreateAccountRequestPayload createAccountRequestPayload(String email, String name, String country, IdentityEntity entity, Boolean CustomerRequested, Boolean merchantRequested, DefaultsResponsibilitiesFeesCollector fees_collector, DefaultsResponsibilitiesLossesCollector losses_collector, Dashboard dashboard,List<String> include ){

        CreateAccountRequestPayload request =
                CreateAccountRequestPayload.builder()
                        .contact_email(email)
                        .display_name(name)
                        .identity(
                                Identity.builder()
                                        .country(country)
                                        .entity_type(entity)
                                        .business_details(
                                                Business_Details.builder()
                                                        .registered_name(name)
                                                        .build()
                                        )
                                        .build()
                        )
                        .configuration(
                                Configuration.builder()
                                        .customer(
                                                Customer.builder()
                                                        .capabilities(
                                                                CustomerCapabilities.builder()
                                                                        .automatic_indirect_tax(
                                                                                Automatic_indirect_tax.builder()
                                                                                        .requested(CustomerRequested)
                                                                                        .build()
                                                                        )
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .merchant(
                                                Merchant.builder()
                                                        .capabilities(
                                                                MerchantCapabilities.builder()
                                                                        .card_payments(
                                                                                Card_Payments.builder()
                                                                                        .requested(merchantRequested)
                                                                                        .build()
                                                                        )
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .defaults(
                                Defaults.builder()
                                        .responsibilities(
                                                Responsibilities.builder()
                                                        .fees_collector(fees_collector)
                                                        .losses_collector(losses_collector)
                                                        .build()
                                        )
                                        .build()
                        )
                        .dashboard(dashboard)
                        .include(include)
                        .build();

        return request;

    }


}



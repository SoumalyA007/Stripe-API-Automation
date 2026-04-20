package datafactory;

import builders.requestbuilder.*;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateAccountDataFactory {

    public static CreateAccountRequestPayload createAccountRequestPayload(){

        CreateAccountRequestPayload request =
                CreateAccountRequestPayload.builder()
                        .contact_email("furever@example.com")
                        .display_name("Furever")
                        .identity(
                                Identity.builder()
                                        .country("us")
                                        .entity_type("company")
                                        .business_details(
                                                Business_Details.builder()
                                                        .registered_name("Furever")
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
                                                                                        .requested(true)
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
                                                                                        .requested(true)
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
                                                        .fees_collector("stripe")
                                                        .losses_collector("stripe")
                                                        .build()
                                        )
                                        .build()
                        )
                        .dashboard("full")
                        .include(List.of(
                                "configuration.merchant",
                                "configuration.customer",
                                "identity",
                                "defaults"
                        ))
                        .build();

        return request;

    }


}



package models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import models.common.BillingDetails;
import models.common.CardDetails;

/**
 * POJO representing the Stripe PaymentMethod API response.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentMethodResponse {

    @NotNull(message = "PaymentMethod id must not be null")
    @Pattern(regexp = "^pm_.*", message = "PaymentMethod id must start with 'pm_'")
    @JsonProperty("id")
    private String id;

    @NotBlank(message = "PaymentMethod 'object' field must not be blank")
    @JsonProperty("object")
    private String object;

    @NotBlank(message = "PaymentMethod 'type' must not be blank")
    @JsonProperty("type")
    private String type;

    @NotNull(message = "PaymentMethod 'created' timestamp must not be null")
    @JsonProperty("created")
    private Long created;

    @NotNull(message = "PaymentMethod 'livemode' must not be null")
    @JsonProperty("livemode")
    private Boolean livemode;

    @JsonProperty("customer")
    private String customer;

    @Valid
    @JsonProperty("billing_details")
    private BillingDetails billingDetails;

    @Valid
    @JsonProperty("card")
    private CardDetails card;
}

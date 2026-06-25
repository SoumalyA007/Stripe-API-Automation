package models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO representing the Stripe Subscription API response.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscriptionResponse {

    @NotNull(message = "Subscription id must not be null")
    @Pattern(regexp = "^sub_.*", message = "Subscription id must start with 'sub_'")
    @JsonProperty("id")
    private String id;

    @NotBlank(message = "Subscription 'object' field must not be blank")
    @JsonProperty("object")
    private String object;

    @NotBlank(message = "Subscription 'status' must not be blank")
    @JsonProperty("status")
    private String status;

    @NotBlank(message = "Subscription 'customer' must not be blank")
    @JsonProperty("customer")
    private String customer;

    @NotNull(message = "Subscription 'created' timestamp must not be null")
    @JsonProperty("created")
    private Long created;

    @NotNull(message = "Subscription 'livemode' must not be null")
    @JsonProperty("livemode")
    private Boolean livemode;

    @JsonProperty("current_period_start")
    private Long currentPeriodStart;

    @JsonProperty("current_period_end")
    private Long currentPeriodEnd;

    @JsonProperty("latest_invoice")
    private String latestInvoice;

    @JsonProperty("default_payment_method")
    private String defaultPaymentMethod;

    @JsonProperty("cancel_at_period_end")
    private Boolean cancelAtPeriodEnd;
}

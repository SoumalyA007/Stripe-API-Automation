package models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO representing the Stripe PaymentIntent API response.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentIntentResponse {

    @NotNull(message = "PaymentIntent id must not be null")
    @Pattern(regexp = "^pi_.*", message = "PaymentIntent id must start with 'pi_'")
    @JsonProperty("id")
    private String id;

    @NotBlank(message = "PaymentIntent 'object' field must not be blank")
    @JsonProperty("object")
    private String object;

    @NotNull(message = "PaymentIntent 'amount' must not be null")
    @Min(value = 1, message = "PaymentIntent 'amount' must be at least 1")
    @JsonProperty("amount")
    private Integer amount;

    @NotBlank(message = "PaymentIntent 'currency' must not be blank")
    @JsonProperty("currency")
    private String currency;

    @NotBlank(message = "PaymentIntent 'status' must not be blank")
    @JsonProperty("status")
    private String status;

    @NotNull(message = "PaymentIntent 'created' timestamp must not be null")
    @JsonProperty("created")
    private Long created;

    @NotNull(message = "PaymentIntent 'livemode' must not be null")
    @JsonProperty("livemode")
    private Boolean livemode;

    @JsonProperty("client_secret")
    private String clientSecret;

    @JsonProperty("customer")
    private String customer;

    @JsonProperty("payment_method")
    private String paymentMethod;

    @JsonProperty("description")
    private String description;

    @JsonProperty("capture_method")
    private String captureMethod;

    @JsonProperty("confirmation_method")
    private String confirmationMethod;

    @JsonProperty("amount_received")
    private Integer amountReceived;

    @JsonProperty("cancellation_reason")
    private String cancellationReason;
}

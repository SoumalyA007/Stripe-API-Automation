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
 * POJO representing the Stripe Refund API response.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefundResponse {

    @NotNull(message = "Refund id must not be null")
    @Pattern(regexp = "^re_.*", message = "Refund id must start with 're_'")
    @JsonProperty("id")
    private String id;

    @NotBlank(message = "Refund 'object' field must not be blank")
    @JsonProperty("object")
    private String object;

    @NotNull(message = "Refund 'amount' must not be null")
    @Min(value = 1, message = "Refund 'amount' must be at least 1")
    @JsonProperty("amount")
    private Integer amount;

    @NotBlank(message = "Refund 'currency' must not be blank")
    @JsonProperty("currency")
    private String currency;

    @NotBlank(message = "Refund 'status' must not be blank")
    @JsonProperty("status")
    private String status;

    @NotNull(message = "Refund 'created' timestamp must not be null")
    @JsonProperty("created")
    private Long created;

    @JsonProperty("charge")
    private String charge;

    @JsonProperty("payment_intent")
    private String paymentIntent;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("description")
    private String description;
}

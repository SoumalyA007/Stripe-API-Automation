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
 * POJO representing the Stripe Payout API response.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayoutResponse {

    @NotNull(message = "Payout id must not be null")
    @Pattern(regexp = "^po_.*", message = "Payout id must start with 'po_'")
    @JsonProperty("id")
    private String id;

    @NotBlank(message = "Payout 'object' field must not be blank")
    @JsonProperty("object")
    private String object;

    @NotNull(message = "Payout 'amount' must not be null")
    @Min(value = 1, message = "Payout 'amount' must be at least 1")
    @JsonProperty("amount")
    private Integer amount;

    @NotBlank(message = "Payout 'currency' must not be blank")
    @JsonProperty("currency")
    private String currency;

    @NotBlank(message = "Payout 'status' must not be blank")
    @JsonProperty("status")
    private String status;

    @NotBlank(message = "Payout 'type' must not be blank")
    @JsonProperty("type")
    private String type;

    @NotNull(message = "Payout 'created' timestamp must not be null")
    @JsonProperty("created")
    private Long created;

    @NotNull(message = "Payout 'livemode' must not be null")
    @JsonProperty("livemode")
    private Boolean livemode;

    @JsonProperty("arrival_date")
    private Long arrivalDate;

    @JsonProperty("method")
    private String method;

    @JsonProperty("description")
    private String description;

    @JsonProperty("destination")
    private String destination;

    @JsonProperty("failure_code")
    private String failureCode;

    @JsonProperty("failure_message")
    private String failureMessage;
}

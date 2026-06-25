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
 * POJO representing the Stripe Transfer API response.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferResponse {

    @NotNull(message = "Transfer id must not be null")
    @Pattern(regexp = "^tr_.*", message = "Transfer id must start with 'tr_'")
    @JsonProperty("id")
    private String id;

    @NotBlank(message = "Transfer 'object' field must not be blank")
    @JsonProperty("object")
    private String object;

    @NotNull(message = "Transfer 'amount' must not be null")
    @Min(value = 1, message = "Transfer 'amount' must be at least 1")
    @JsonProperty("amount")
    private Integer amount;

    @NotBlank(message = "Transfer 'currency' must not be blank")
    @JsonProperty("currency")
    private String currency;

    @NotBlank(message = "Transfer 'destination' must not be blank")
    @JsonProperty("destination")
    private String destination;

    @NotNull(message = "Transfer 'created' timestamp must not be null")
    @JsonProperty("created")
    private Long created;

    @NotNull(message = "Transfer 'livemode' must not be null")
    @JsonProperty("livemode")
    private Boolean livemode;

    @JsonProperty("amount_reversed")
    private Integer amountReversed;

    @JsonProperty("reversed")
    private Boolean reversed;

    @JsonProperty("destination_payment")
    private String destinationPayment;

    @JsonProperty("description")
    private String description;
}

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
 * POJO representing the Stripe TransferReversal API response.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferReversalResponse {

    @NotNull(message = "TransferReversal id must not be null")
    @Pattern(regexp = "^trr_.*", message = "TransferReversal id must start with 'trr_'")
    @JsonProperty("id")
    private String id;

    @NotBlank(message = "TransferReversal 'object' field must not be blank")
    @JsonProperty("object")
    private String object;

    @NotNull(message = "TransferReversal 'amount' must not be null")
    @Min(value = 1, message = "TransferReversal 'amount' must be at least 1")
    @JsonProperty("amount")
    private Integer amount;

    @NotBlank(message = "TransferReversal 'currency' must not be blank")
    @JsonProperty("currency")
    private String currency;

    @NotBlank(message = "TransferReversal 'transfer' must not be blank")
    @JsonProperty("transfer")
    private String transfer;

    @NotNull(message = "TransferReversal 'created' timestamp must not be null")
    @JsonProperty("created")
    private Long created;

    @JsonProperty("description")
    private String description;
}

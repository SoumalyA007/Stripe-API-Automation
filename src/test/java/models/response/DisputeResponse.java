package models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DisputeResponse {

    @NotBlank(message = "Dispute 'id' must not be blank")
    @JsonProperty("id")
    private String id;

    @NotBlank(message = "Dispute 'object' field must not be blank")
    @JsonProperty("object")
    private String object;

    @NotNull(message = "Dispute 'amount' must not be null")
    @JsonProperty("amount")
    private Integer amount;

    @NotBlank(message = "Dispute 'currency' must not be blank")
    @JsonProperty("currency")
    private String currency;

    @NotBlank(message = "Dispute 'status' must not be blank")
    @JsonProperty("status")
    private String status;

    @NotNull(message = "Dispute 'created' timestamp must not be null")
    @JsonProperty("created")
    private Long created;

    @NotNull(message = "Dispute 'livemode' must not be null")
    @JsonProperty("livemode")
    private Boolean livemode;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("charge")
    private String charge;

    @JsonProperty("payment_intent")
    private String paymentIntent;

    @JsonProperty("evidence")
    private Map<String, Object> evidence;
}

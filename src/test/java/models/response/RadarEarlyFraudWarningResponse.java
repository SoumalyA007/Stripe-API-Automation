package models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO representing the Stripe Radar Early Fraud Warning API response.
 * Object type: "radar.early_fraud_warning"
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RadarEarlyFraudWarningResponse {

    @NotBlank(message = "RadarEarlyFraudWarning 'id' must not be blank")
    @JsonProperty("id")
    private String id;

    @NotBlank(message = "RadarEarlyFraudWarning 'object' must not be blank")
    @JsonProperty("object")
    private String object;

    @NotNull(message = "RadarEarlyFraudWarning 'created' timestamp must not be null")
    @JsonProperty("created")
    private Long created;

    @NotNull(message = "RadarEarlyFraudWarning 'livemode' must not be null")
    @JsonProperty("livemode")
    private Boolean livemode;

    @JsonProperty("actionable")
    private Boolean actionable;

    @JsonProperty("charge")
    private String charge;

    @JsonProperty("payment_intent")
    private String paymentIntent;

    @JsonProperty("fraud_type")
    private String fraudType;
}

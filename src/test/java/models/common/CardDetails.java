package models.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents Stripe card details nested in a PaymentMethod response.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDetails {

    @JsonProperty("brand")
    private String brand;

    @JsonProperty("country")
    private String country;

    @JsonProperty("exp_month")
    private Integer expMonth;

    @JsonProperty("exp_year")
    private Integer expYear;

    @JsonProperty("fingerprint")
    private String fingerprint;

    @JsonProperty("funding")
    private String funding;

    @JsonProperty("last4")
    private String last4;
}

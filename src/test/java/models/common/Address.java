package models.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a Stripe Address object, nested inside BillingDetails.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {

    @JsonProperty("city")
    private String city;

    @JsonProperty("country")
    private String country;

    @JsonProperty("line1")
    private String line1;

    @JsonProperty("line2")
    private String line2;

    @JsonProperty("postal_code")
    private String postalCode;

    @JsonProperty("state")
    private String state;
}

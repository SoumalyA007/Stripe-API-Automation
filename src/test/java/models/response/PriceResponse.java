package models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import models.common.RecurringInfo;

/**
 * POJO representing the Stripe Price API response.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceResponse {

    @NotNull(message = "Price id must not be null")
    @Pattern(regexp = "^price_.*", message = "Price id must start with 'price_'")
    @JsonProperty("id")
    private String id;

    @NotBlank(message = "Price 'object' field must not be blank")
    @JsonProperty("object")
    private String object;

    @NotBlank(message = "Price 'currency' must not be blank")
    @JsonProperty("currency")
    private String currency;

    @NotNull(message = "Price 'active' must not be null")
    @JsonProperty("active")
    private Boolean active;

    @NotNull(message = "Price 'created' timestamp must not be null")
    @JsonProperty("created")
    private Long created;

    @NotNull(message = "Price 'livemode' must not be null")
    @JsonProperty("livemode")
    private Boolean livemode;

    @JsonProperty("unit_amount")
    private Integer unitAmount;

    @JsonProperty("product")
    private String product;

    @JsonProperty("type")
    private String type;

    @Valid
    @JsonProperty("recurring")
    private RecurringInfo recurring;
}

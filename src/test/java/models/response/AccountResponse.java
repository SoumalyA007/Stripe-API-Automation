package models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO representing the Stripe Account API response.
 * Validated with Bean Validation annotations to assert structural correctness.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountResponse {

    @NotNull(message = "Account id must not be null")
    @Pattern(regexp = "^acct_.*", message = "Account id must start with 'acct_'")
    @JsonProperty("id")
    private String id;

    @NotBlank(message = "Account 'object' field must not be blank")
    @JsonProperty("object")
    private String object;

    @NotNull(message = "Account 'created' timestamp must not be null")
    @JsonProperty("created")
    private Long created;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("contact_email")
    private String contactEmail;

    @JsonProperty("default_currency")
    private String defaultCurrency;

    @JsonProperty("country")
    private String country;

    @JsonProperty("livemode")
    private Boolean livemode;
}

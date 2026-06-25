package models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO representing the Stripe ConnectedAccount (Express/Custom) API response.
 * The 'object' value is "account" — same as a platform account.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConnectedAccountResponse {

    @NotNull(message = "ConnectedAccount id must not be null")
    @Pattern(regexp = "^acct_.*", message = "ConnectedAccount id must start with 'acct_'")
    @JsonProperty("id")
    private String id;

    @NotBlank(message = "ConnectedAccount 'object' field must not be blank")
    @JsonProperty("object")
    private String object;

    @NotNull(message = "ConnectedAccount 'created' timestamp must not be null")
    @JsonProperty("created")
    private Long created;

    @JsonProperty("type")
    private String type;

    @JsonProperty("email")
    private String email;

    @JsonProperty("country")
    private String country;

    @JsonProperty("default_currency")
    private String defaultCurrency;
}

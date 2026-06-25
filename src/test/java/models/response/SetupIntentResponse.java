package models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * POJO representing the Stripe SetupIntent API response.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SetupIntentResponse {

    @NotNull(message = "SetupIntent id must not be null")
    @Pattern(regexp = "^seti_.*", message = "SetupIntent id must start with 'seti_'")
    @JsonProperty("id")
    private String id;

    @NotBlank(message = "SetupIntent 'object' field must not be blank")
    @JsonProperty("object")
    private String object;

    @NotBlank(message = "SetupIntent 'status' must not be blank")
    @JsonProperty("status")
    private String status;

    @NotBlank(message = "SetupIntent 'usage' must not be blank")
    @JsonProperty("usage")
    private String usage;

    @NotNull(message = "SetupIntent 'created' timestamp must not be null")
    @JsonProperty("created")
    private Long created;

    @NotNull(message = "SetupIntent 'livemode' must not be null")
    @JsonProperty("livemode")
    private Boolean livemode;

    @JsonProperty("client_secret")
    private String clientSecret;

    @JsonProperty("customer")
    private String customer;

    @JsonProperty("payment_method")
    private String paymentMethod;

    @JsonProperty("payment_method_types")
    private List<String> paymentMethodTypes;

    @JsonProperty("cancellation_reason")
    private String cancellationReason;

    @JsonProperty("description")
    private String description;
}

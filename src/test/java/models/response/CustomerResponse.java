package models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO representing the Stripe Customer API response.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerResponse {

    @NotNull(message = "Customer id must not be null")
    @Pattern(regexp = "^cus_.*", message = "Customer id must start with 'cus_'")
    @JsonProperty("id")
    private String id;

    @NotBlank(message = "Customer 'object' field must not be blank")
    @JsonProperty("object")
    private String object;

    @NotNull(message = "Customer 'created' timestamp must not be null")
    @JsonProperty("created")
    private Long created;

    @NotNull(message = "Customer 'livemode' must not be null")
    @JsonProperty("livemode")
    private Boolean livemode;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("description")
    private String description;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("balance")
    private Integer balance;

    @JsonProperty("delinquent")
    private Boolean delinquent;
}

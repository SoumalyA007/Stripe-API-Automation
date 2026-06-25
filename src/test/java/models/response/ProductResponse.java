package models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO representing the Stripe Product API response.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductResponse {

    @NotNull(message = "Product id must not be null")
    @Pattern(regexp = "^prod_.*", message = "Product id must start with 'prod_'")
    @JsonProperty("id")
    private String id;

    @NotBlank(message = "Product 'object' field must not be blank")
    @JsonProperty("object")
    private String object;

    @NotBlank(message = "Product 'name' must not be blank")
    @JsonProperty("name")
    private String name;

    @NotNull(message = "Product 'active' must not be null")
    @JsonProperty("active")
    private Boolean active;

    @NotNull(message = "Product 'created' timestamp must not be null")
    @JsonProperty("created")
    private Long created;

    @NotNull(message = "Product 'livemode' must not be null")
    @JsonProperty("livemode")
    private Boolean livemode;

    @JsonProperty("description")
    private String description;

    @JsonProperty("type")
    private String type;

    @JsonProperty("updated")
    private Long updated;
}

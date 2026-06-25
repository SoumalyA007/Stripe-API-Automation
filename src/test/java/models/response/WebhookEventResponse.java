package models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import models.common.WebhookEventData;

/**
 * POJO representing the Stripe Event (Webhook Event) API response.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookEventResponse {

    @NotNull(message = "Event id must not be null")
    @Pattern(regexp = "^evt_.*", message = "Event id must start with 'evt_'")
    @JsonProperty("id")
    private String id;

    @NotBlank(message = "Event 'object' field must not be blank")
    @JsonProperty("object")
    private String object;

    @NotBlank(message = "Event 'type' must not be blank")
    @JsonProperty("type")
    private String type;

    @NotNull(message = "Event 'created' timestamp must not be null")
    @JsonProperty("created")
    private Long created;

    @NotNull(message = "Event 'livemode' must not be null")
    @JsonProperty("livemode")
    private Boolean livemode;

    @NotNull(message = "Event 'data' must not be null")
    @Valid
    @JsonProperty("data")
    private WebhookEventData data;

    @JsonProperty("api_version")
    private String apiVersion;

    @JsonProperty("pending_webhooks")
    private Integer pendingWebhooks;
}

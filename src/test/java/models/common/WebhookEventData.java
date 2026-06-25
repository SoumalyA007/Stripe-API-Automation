package models.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents the {@code data} envelope inside a Stripe webhook Event object.
 * The inner {@code object} is an arbitrary Stripe resource deserialized as a generic map.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookEventData {

    @JsonProperty("object")
    private Map<String, Object> object;
}

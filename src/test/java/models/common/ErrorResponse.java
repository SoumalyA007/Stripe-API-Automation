package models.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Top-level wrapper for a Stripe error response body.
 * <pre>
 * {
 *   "error": { "type": "invalid_request_error", "message": "...", ... }
 * }
 * </pre>
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse {

    @NotNull(message = "Error response must contain an 'error' object")
    @Valid
    @JsonProperty("error")
    private ErrorDetail error;
}

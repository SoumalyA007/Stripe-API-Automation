package models.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the nested {@code error} object inside a Stripe error response.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorDetail {

    @JsonProperty("type")
    private String type;

    @JsonProperty("message")
    private String message;

    @JsonProperty("code")
    private String code;

    @JsonProperty("param")
    private String param;

    @JsonProperty("doc_url")
    private String docUrl;

    @JsonProperty("decline_code")
    private String declineCode;

    @JsonProperty("charge")
    private String charge;
}

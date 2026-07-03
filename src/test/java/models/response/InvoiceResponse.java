package models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO representing the Stripe Invoice API response.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceResponse {

    @NotNull(message = "Invoice id must not be null")
    @Pattern(regexp = "^in_.*", message = "Invoice id must start with 'in_'")
    @JsonProperty("id")
    private String id;

    @NotBlank(message = "Invoice 'object' field must not be blank")
    @JsonProperty("object")
    private String object;

    @NotBlank(message = "Invoice 'status' must not be blank")
    @JsonProperty("status")
    private String status;

    @NotBlank(message = "Invoice 'customer' must not be blank")
    @JsonProperty("customer")
    private String customer;

    @NotBlank(message = "Invoice 'currency' must not be blank")
    @JsonProperty("currency")
    private String currency;

    @NotNull(message = "Invoice 'amount_due' must not be null")
    @Min(value = 0, message = "Invoice 'amount_due' must be >= 0")
    @JsonProperty("amount_due")
    private Integer amountDue;

    @NotNull(message = "Invoice 'amount_paid' must not be null")
    @JsonProperty("amount_paid")
    private Integer amountPaid;

    @NotNull(message = "Invoice 'amount_remaining' must not be null")
    @JsonProperty("amount_remaining")
    private Integer amountRemaining;

    @NotNull(message = "Invoice 'created' timestamp must not be null")
    @JsonProperty("created")
    private Long created;

    @NotNull(message = "Invoice 'livemode' must not be null")
    @JsonProperty("livemode")
    private Boolean livemode;

    @JsonProperty("subscription")
    private String subscription;

    @JsonProperty("hosted_invoice_url")
    private String hostedInvoiceUrl;

    @JsonProperty("invoice_pdf")
    private String invoicePdf;

    @JsonProperty("description")
    private String description;

    @JsonProperty("due_date")
    private Long dueDate;
}

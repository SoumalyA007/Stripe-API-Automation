package helpers;

import endpoints.Invoices;
import specification.ResponseSpec;
import testbase.BaseClass;

import java.util.HashMap;
import java.util.Map;

public class InvoicesHelper {

    /**
     * Creates a customer, adds an invoice item, creates an invoice, and returns the
     * draft invoice ID. Useful as a fallback when no invoice exists in context.
     *
     * @return the draft invoice ID
     */
    public static String createFallbackDraftInvoice() {
        String customerId = TestContext.getCustomerId();
        if (customerId == null) {
            customerId = CustomersHelper.createCustomer();
            TestContext.setCustomerId(customerId);
        }

        // An invoice must have at least one line item before it can be created
        addInvoiceItem(customerId, BaseClass.amount);

        Map<String, Object> body = new HashMap<>();
        body.put("customer", customerId);

        return Invoices.createInvoice(body)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("id");
    }

    /**
     * Creates a customer, adds an invoice item, creates a draft invoice,
     * finalizes it (status → open), and returns the open invoice ID.
     *
     * @return the open invoice ID
     */
    public static String createFallbackOpenInvoice() {
        String invoiceId = createFallbackDraftInvoice();
        return Invoices.finalizeInvoice(invoiceId)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("id");
    }

    /**
     * Creates an open invoice and pays it (status → paid).
     *
     * @return the paid invoice ID
     */
    public static String createFallbackPaidInvoice() {
        String paymentMethodId = TestContext.getPaymentMethodId();
        if (paymentMethodId == null) {
            paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(true);
        }

        String invoiceId = createFallbackOpenInvoice();

        Map<String, Object> payBody = new HashMap<>();
        payBody.put("payment_method", paymentMethodId);

        return Invoices.payInvoice(invoiceId, payBody)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("id");
    }

    /**
     * Adds an invoice item (a line item) to the given customer.
     * An invoice must have at least one item before creation.
     */
    public static void addInvoiceItem(String customerId, int unitAmount) {
        Map<String, Object> body = new HashMap<>();
        body.put("customer", customerId);
        body.put("amount", unitAmount);
        body.put("currency", "usd");
        body.put("description", "Automation test item");

        Invoices.createInvoiceItem(body)
                .then()
                .spec(ResponseSpec.OK());
    }
}

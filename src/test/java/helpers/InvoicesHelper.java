package helpers;

import java.util.HashMap;
import java.util.Map;

import endpoints.Invoices;
import specification.ResponseSpec;
import testbase.BaseClass;

public class InvoicesHelper {

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
        body.put("pending_invoice_items_behavior", "include");

        return Invoices.createInvoice(body)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("id");
    }

    public static String createFallbackOpenInvoice() {
        String invoiceId = createFallbackDraftInvoice();
        return Invoices.finalizeInvoice(invoiceId)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("id");
    }

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

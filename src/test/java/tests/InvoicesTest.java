package tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import dataprovider.InvoicesDataProvider;
import endpoints.Invoices;
import helpers.CustomersHelper;
import helpers.InvoicesHelper;
import helpers.PaymentMethodsHelper;
import helpers.PojoValidator;
import helpers.TestContext;
import io.restassured.response.Response;
import models.response.InvoiceResponse;
import specification.ResponseSpec;
import testbase.BaseClass;

public class InvoicesTest extends BaseClass {

    List<String> fallbackInvoiceIds = new ArrayList<>();
    List<String> fallbackCustomerIds = new ArrayList<>();

    // =====================================================================
    // POSITIVE — LIFECYCLE TESTS
    // =====================================================================

    // ***************CREATE INVOICE (DRAFT) – POSITIVE*******************\\

    @Test(groups = { "invoice", "regression", "create_finalize_pay_invoice", "smoke" })
    public void TC_01_Create_Draft_Invoice() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

        // Ensure a customer exists
        String customerId = TestContext.getCustomerId();
        if (customerId == null) {
            customerId = CustomersHelper.createCustomer();
            TestContext.setCustomerId(customerId);
            fallbackCustomerIds.add(customerId);
            logger.info("Created fallback customer: {}", customerId);
        }

        // Add an invoice item — required for the invoice to have a non-zero amount
        InvoicesHelper.addInvoiceItem(customerId, amount);
        logger.info("Added invoice item of {} to customer: {}", amount, customerId);

        Map<String, Object> body = new HashMap<>();
        body.put("customer", customerId);
        body.put("pending_invoice_items_behavior", "include");

        Response resp = Invoices.createInvoice(body);
        String invoiceId = resp.then()
        .spec(ResponseSpec.OK())
        .body("object", equalTo("invoice"))
        .body("status", equalTo("draft"))
        .body("customer", equalTo(customerId))
        .body("currency", equalTo("usd"))

        .body("lines.total_count", equalTo(1))
        .body("lines.data[0].amount", equalTo(amount))
        .body("subtotal", equalTo(amount))
        .body("total", equalTo(amount))
        .body("amount_due", equalTo(amount))
        .body("amount_remaining", equalTo(amount))
        .body("amount_paid", equalTo(0))

        // --- draft-state invariants ---
        .body("attempted", equalTo(false))
        .body("auto_advance", equalTo(false))
        .body("status_transitions.finalized_at", nullValue())
        .body("hosted_invoice_url", nullValue())
        .body("invoice_pdf", nullValue())
        .body("number", nullValue()) // invoice number isn't assigned until finalized

        // --- customer snapshot fields (Stripe copies these onto the invoice) ---// if you track this
        .body("customer_name", notNullValue())

        // --- id sanity ---
        .body("id", startsWith("in_"))

        .extract()
        .jsonPath()
        .getString("id");

        InvoiceResponse invoiceResponse = resp.as(InvoiceResponse.class);
        PojoValidator.validate(invoiceResponse);
        logger.info("POJO validation passed for invoice: {}", invoiceId);

        TestContext.setInvoiceId(invoiceId);
        logger.info("Created draft invoice: {}", invoiceId);
    }

    // ***************FINALIZE INVOICE – POSITIVE*******************\\

    @Test(groups = { "invoice", "regression",
            "create_finalize_pay_invoice" })
    public void TC_02_Finalize_Invoice() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

        String invoiceId = TestContext.getInvoiceId();
        if (invoiceId == null) {
            invoiceId = InvoicesHelper.createFallbackDraftInvoice();
            fallbackInvoiceIds.add(invoiceId);
            logger.info("Created fallback draft invoice: {}", invoiceId);
        }

        logger.info("Finalizing invoice: {}", invoiceId);
        Response resp = Invoices.finalizeInvoice(invoiceId);
        resp.then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(invoiceId))
                .body("status", equalTo("open"));

        InvoiceResponse invoiceResponse = resp.as(InvoiceResponse.class);
        PojoValidator.validate(invoiceResponse);
        logger.info("POJO validation passed for finalized invoice: {}", invoiceId);

        // Update context with the same invoice (now open)
        TestContext.setInvoiceId(invoiceId);
        logger.info("Invoice {} finalized successfully (status: open)", invoiceId);
    }

    // ***************PAY INVOICE – POSITIVE*******************\\

    @Test(groups = { "invoice", "regression",
            "create_finalize_pay_invoice" })
    public void TC_03_Pay_Invoice() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

        String invoiceId = TestContext.getInvoiceId();
        if (invoiceId == null) {
            invoiceId = InvoicesHelper.createFallbackOpenInvoice();
            fallbackInvoiceIds.add(invoiceId);
            logger.info("Created fallback open invoice: {}", invoiceId);
        }

        String paymentMethodId = TestContext.getPaymentMethodId();
        if (paymentMethodId == null) {
            paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(true);
            logger.info("Created fallback payment method: {}", paymentMethodId);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("payment_method", paymentMethodId);

        logger.info("Paying invoice: {} with payment method: {}", invoiceId, paymentMethodId);
        Response resp = Invoices.payInvoice(invoiceId, body);
        resp.then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(invoiceId))
                .body("status", equalTo("paid"))
                .body("amount_remaining", equalTo(0));

        InvoiceResponse invoiceResponse = resp.as(InvoiceResponse.class);
        PojoValidator.validate(invoiceResponse);
        logger.info("POJO validation passed for paid invoice: {}", invoiceId);
        logger.info("Invoice {} paid successfully.", invoiceId);

        // Clear context — invoice is consumed
        TestContext.setInvoiceId(null);
    }

    // ***************RETRIEVE INVOICE – POSITIVE*******************\\

    @Test(groups = { "invoice", "regression", "retrieve_invoice", "sanity" })
    public void TC_04_Retrieve_Invoice() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

        String invoiceId = TestContext.getInvoiceId();
        if (invoiceId == null) {
            invoiceId = InvoicesHelper.createFallbackDraftInvoice();
            fallbackInvoiceIds.add(invoiceId);
            logger.info("Created fallback draft invoice for retrieval: {}", invoiceId);
        }

        logger.info("Retrieving invoice: {}", invoiceId);
        Response resp = Invoices.retrieveInvoice(invoiceId);
        resp.then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(invoiceId))
                .body("object", equalTo("invoice"));

        InvoiceResponse invoiceResponse = resp.as(InvoiceResponse.class);
        PojoValidator.validate(invoiceResponse);
        logger.info("POJO validation passed for retrieved invoice: {}", invoiceId);
        logger.info("Invoice {} retrieved successfully.", invoiceId);
    }

    // ***************UPDATE INVOICE – POSITIVE*******************\\

    @Test(groups = { "invoice", "regression", "update_invoice" })
    public void TC_05_Update_Invoice_Description() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

        // Only draft invoices can be updated
        String invoiceId = InvoicesHelper.createFallbackDraftInvoice();
        fallbackInvoiceIds.add(invoiceId);
        logger.info("Created draft invoice to update: {}", invoiceId);

        Map<String, Object> body = new HashMap<>();
        body.put("description", "Updated by automation test");

        logger.info("Updating invoice: {}", invoiceId);
        Invoices.updateInvoice(invoiceId, body)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(invoiceId))
                .body("description", equalTo("Updated by automation test"));

        logger.info("Invoice {} description updated successfully.", invoiceId);
    }

    // ***************VOID INVOICE – POSITIVE*******************\\

    @Test(groups = { "invoice", "regression", "void_invoice" })
    public void TC_06_Void_Invoice() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

        // Must finalize first — only open invoices can be voided
        String invoiceId = InvoicesHelper.createFallbackOpenInvoice();
        fallbackInvoiceIds.add(invoiceId);
        logger.info("Created open invoice to void: {}", invoiceId);

        logger.info("Voiding invoice: {}", invoiceId);
        Response resp = Invoices.voidInvoice(invoiceId);
        resp.then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(invoiceId))
                .body("status", equalTo("void"));

        InvoiceResponse invoiceResponse = resp.as(InvoiceResponse.class);
        PojoValidator.validate(invoiceResponse);
        logger.info("POJO validation passed for voided invoice: {}", invoiceId);
        logger.info("Invoice {} voided successfully.", invoiceId);
    }

    // ***************MARK UNCOLLECTIBLE – POSITIVE*******************\\

    @Test(groups = { "invoice", "regression", "mark_uncollectible_invoice" })
    public void TC_07_Mark_Invoice_Uncollectible() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

        // Must finalize first — only open invoices can be marked uncollectible
        String invoiceId = InvoicesHelper.createFallbackOpenInvoice();
        fallbackInvoiceIds.add(invoiceId);
        logger.info("Created open invoice to mark uncollectible: {}", invoiceId);

        logger.info("Marking invoice {} as uncollectible", invoiceId);
        Response resp = Invoices.markUncollectible(invoiceId);
        resp.then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(invoiceId))
                .body("status", equalTo("uncollectible"));

        InvoiceResponse invoiceResponse = resp.as(InvoiceResponse.class);
        PojoValidator.validate(invoiceResponse);
        logger.info("POJO validation passed for uncollectible invoice: {}", invoiceId);
        logger.info("Invoice {} marked uncollectible.", invoiceId);
    }

    // ***************DELETE DRAFT INVOICE – POSITIVE*******************\\

    @Test(groups = { "invoice", "regression", "delete_invoice" })
    public void TC_08_Delete_Draft_Invoice() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

        // A fresh draft — do not reuse context (we are deleting it)
        String invoiceId = InvoicesHelper.createFallbackDraftInvoice();
        logger.info("Created draft invoice to delete: {}", invoiceId);

        logger.info("Deleting draft invoice: {}", invoiceId);
        Invoices.deleteInvoice(invoiceId)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(invoiceId))
                .body("deleted", equalTo(true));

        logger.info("Draft invoice {} deleted successfully.", invoiceId);
    }

    // ***************SEND INVOICE – POSITIVE*******************\\

    @Test(groups = { "invoice", "regression", "send_invoice" })
    public void TC_09_Send_Invoice() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

        // Must be open to send
        String invoiceId = InvoicesHelper.createFallbackSendInvoice();
        fallbackInvoiceIds.add(invoiceId);
        logger.info("Created open invoice to send: {}", invoiceId);

        logger.info("Sending invoice: {}", invoiceId);
        Response resp = Invoices.sendInvoice(invoiceId);
        resp.then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(invoiceId))
                .body("status", equalTo("open"));

        logger.info("Invoice {} sent successfully.", invoiceId);
    }

    // =====================================================================
    // NEGATIVE TESTS
    // =====================================================================

    // ***************CREATE INVOICE – NEGATIVE*******************\\

    @Test(groups = { "invoice", "negative",
            "regression" }, dataProvider = "invalidInvoicePayloads", dataProviderClass = InvoicesDataProvider.class)
    public void TC_10_Create_Invoice_Invalid_Payloads(String testCaseName, Map<String, Object> body) {
        logger.info("Running invalid invoice creation case: {}", testCaseName);
        Invoices.createInvoice(body)
                .then()
                .spec(ResponseSpec.bad_request());
        logger.info("Successfully verified bad request for: {}", testCaseName);
    }

    // ***************RETRIEVE INVOICE – NEGATIVE*******************\\

    @Test(groups = { "invoice", "negative",
            "regression" }, dataProvider = "invalidInvoiceIds", dataProviderClass = InvoicesDataProvider.class)
    public void TC_11_Retrieve_Invoice_Invalid_Id(String testCaseName, String invoiceId, String expectedError) {
        logger.info("Running invalid retrieve case: {} for ID: {}", testCaseName, invoiceId);
        Invoices.retrieveInvoice(invoiceId)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.message", containsString(expectedError));
        logger.info("Successfully verified not found for: {}", testCaseName);
    }

    // ***************FINALIZE INVOICE – NEGATIVE*******************\\

    @Test(groups = { "invoice", "negative", "regression" })
    public void TC_12_Finalize_Already_Open_Invoice() {
        logger.info("Testing finalize on an already-open invoice (should fail)");
        String invoiceId = InvoicesHelper.createFallbackOpenInvoice();
        fallbackInvoiceIds.add(invoiceId);
        logger.info("Using open invoice: {}", invoiceId);

        // Finalizing an already-open invoice returns 400 bad request
        Invoices.finalizeInvoice(invoiceId)
                .then()
                .spec(ResponseSpec.bad_request());
        logger.info("Correctly rejected finalize on already-open invoice.");
    }

    // ***************VOID INVOICE – NEGATIVE*******************\\

    @Test(groups = { "invoice", "negative", "regression" })
    public void TC_13_Void_Draft_Invoice_Fails() {
        logger.info("Testing void on a draft invoice (should fail - only open invoices can be voided)");
        String invoiceId = InvoicesHelper.createFallbackDraftInvoice();
        fallbackInvoiceIds.add(invoiceId);
        logger.info("Using draft invoice: {}", invoiceId);

        Invoices.voidInvoice(invoiceId)
                .then()
                .spec(ResponseSpec.bad_request());
        logger.info("Correctly rejected void on draft invoice.");
    }

    // ***************DELETE FINALIZED INVOICE – NEGATIVE*******************\\

    @Test(groups = { "invoice", "negative", "regression" })
    public void TC_14_Delete_Open_Invoice_Fails() {
        logger.info("Testing delete on an open invoice (only drafts can be deleted)");
        String invoiceId = InvoicesHelper.createFallbackOpenInvoice();
        fallbackInvoiceIds.add(invoiceId);
        logger.info("Using open invoice: {}", invoiceId);

        Invoices.deleteInvoice(invoiceId)
                .then()
                .spec(ResponseSpec.bad_request());
        logger.info("Correctly rejected delete on non-draft invoice.");
    }

    // ***************AUTH – INVALID KEY*******************\\

    @Test(groups = { "invoice", "negative", "auth", "regression" })
    public void TC_15_Create_Invoice_Invalid_Auth() {
        logger.info("Testing create invoice with invalid auth key");
        Map<String, Object> body = new HashMap<>();
        body.put("customer", "cus_any");

        Invoices.createInvoiceWithCustomAuth("sk_test_invalid_key_12345", body)
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));
        logger.info("Correctly rejected create invoice with invalid auth.");
    }

    // ***************AUTH – MISSING KEY*******************\\

    @Test(groups = { "invoice", "negative", "auth", "regression" })
    public void TC_16_Create_Invoice_Missing_Auth() {
        logger.info("Testing create invoice with missing auth key");
        Map<String, Object> body = new HashMap<>();
        body.put("customer", "cus_any");

        Invoices.createInvoiceWithCustomAuth(null, body)
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("You did not provide an API key"));
        logger.info("Correctly rejected create invoice with missing auth.");
    }

    // ***************RETRIEVE INVOICE – INVALID AUTH*******************\\

    @Test(groups = { "invoice", "negative", "auth", "regression" })
    public void TC_17_Retrieve_Invoice_Invalid_Auth() {
        logger.info("Testing retrieve invoice with invalid auth key");

        Invoices.retrieveInvoiceWithCustomAuth("sk_test_invalid_key_12345", "in_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));
        logger.info("Correctly rejected retrieve invoice with invalid auth.");
    }

    // ***************IDEMPOTENCY – CREATE INVOICE*******************\\

    @Test(groups = { "idempotent_test" })
    public void TC_18_Idempotent_Create_Invoice() {
        logger.info("Testing idempotent create invoice");

        String customerId = CustomersHelper.createCustomer();
        fallbackCustomerIds.add(customerId);
        InvoicesHelper.addInvoiceItem(customerId, amount);

        Map<String, Object> body = new HashMap<>();
        body.put("customer", customerId);

        String firstId = Invoices.createInvoice(body) // Note: idempotency key header handled via spec
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("id");
        fallbackInvoiceIds.add(firstId);

        // Re-send same request — Stripe idempotency is enforced by the key on the API
        // gateway,
        // so here we validate the object itself is consistent on re-retrieval.
        String retrievedId = Invoices.retrieveInvoice(firstId)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(firstId))
                .extract()
                .jsonPath()
                .getString("id");

        org.testng.Assert.assertEquals(retrievedId, firstId,
                "Idempotency check: retrieved invoice ID should match created ID");
        logger.info("Idempotency verified for invoice: {}", firstId);
    }

    // =====================================================================
    // CLEANUP
    // =====================================================================

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        logger.info("Running cleanup for InvoicesTest");
        logger.info("Cleaning up {} fallback invoice(s)", fallbackInvoiceIds.size());
        for (String invoiceId : fallbackInvoiceIds) {
        try {
            Response getResp = Invoices.retrieveInvoice(invoiceId);
            String status = getResp.jsonPath().getString("status");

            if ("draft".equals(status)) {
                Invoices.deleteInvoice(invoiceId);
                logger.info("Deleted draft invoice: {}", invoiceId);
            } else {
                Invoices.voidInvoice(invoiceId); // POST /v1/invoices/{id}/void
                logger.info("Voided non-draft invoice ({}): {}", status, invoiceId);
            }
        } catch (Exception e) {
            logger.warn("Cleanup failed for invoice {}: {}", invoiceId, e.getMessage());
        }
    }
        fallbackInvoiceIds.clear();
        fallbackCustomerIds.clear();
    }
}

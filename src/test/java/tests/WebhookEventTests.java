package tests;

import endpoints.Customer;
import endpoints.Events;
import endpoints.Refunds;
import helpers.CustomersHelper;
import helpers.PaymentIntentHelper;
import helpers.PojoValidator;
import helpers.TestContext;
import io.restassured.response.Response;
import models.response.WebhookEventResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import specification.ResponseSpec;
import testbase.BaseClass;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class WebhookEventTests extends BaseClass {

    List<String> fallbackCustomerIds = new ArrayList<>();
    List<String> fallbackPaymentIntentIds = new ArrayList<>();

    // Helper method to poll Stripe Events API for a specific event type and object
    // ID
    private String pollForEvent(String eventType, String targetObjectId) {
        int maxRetries = 6;
        long delayMillis = 1500;

        logger.info("Polling for event type [{}] with target object ID [{}]...", eventType, targetObjectId);

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("type", eventType);
            queryParams.put("limit", 10);

            Response response = Events.getEvents(queryParams);
            if (response.getStatusCode() == 200) {
                List<Map<String, Object>> events = response.jsonPath().getList("data");
                for (Map<String, Object> event : events) {
                    Map<String, Object> data = (Map<String, Object>) event.get("data");
                    if (data != null) {
                        Map<String, Object> object = (Map<String, Object>) data.get("object");
                        if (object != null && targetObjectId.equals(object.get("id"))) {
                            String eventId = (String) event.get("id");
                            logger.info("Found event matching target object ID [{}] on attempt {}: Event ID = {}",
                                    targetObjectId, attempt, eventId);
                            return eventId;
                        }
                    }
                }
            } else {
                logger.warn("Received status code {} while polling events.", response.getStatusCode());
            }

            logger.info("Attempt {}/{} failed. Waiting {}ms...", attempt, maxRetries, delayMillis);
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Polling interrupted", e);
            }
        }
        return null;
    }

    @Test(groups = { "webhook", "regression", "marketplace_e2e", "smoke" })
    public void TC_01_Verify_CustomerCreated_Event() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

        String customerId = TestContext.getCustomerId();
        if (customerId == null) {
            customerId = CustomersHelper.createCustomer();
            fallbackCustomerIds.add(customerId);
            logger.info("Created new Customer ID --> {}", customerId);
        } else {
            logger.info("Fetched customer ID from context --> {}", customerId);
        }

        // Retrieve customer to get name/email for event body assertions
        Response customerResponse = Customer.getCustomer(customerId)
                .then().spec(ResponseSpec.OK()).extract().response();
        String name = customerResponse.jsonPath().getString("name");
        String email = customerResponse.jsonPath().getString("email");

        // Poll for customer.created event
        String eventId = pollForEvent("customer.created", customerId);
        assertThat("Event customer.created was not found in the events log", eventId, notNullValue());

        // Validate the event object details
        logger.info("Retrieving event: {}", eventId);
        Response eventResp = Events.getEvent(eventId);
        eventResp.then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(eventId))
                .body("type", equalTo("customer.created"))
                .body("data.object.id", equalTo(customerId))
                .body("data.object.email", equalTo(email))
                .body("data.object.name", equalTo(name));

        WebhookEventResponse eventResponse = eventResp.as(WebhookEventResponse.class);
        PojoValidator.validate(eventResponse);
        logger.info("POJO validation passed for webhook event: {}", eventId);
        logger.info("Successfully validated customer.created event details");
    }

    @Test(groups = { "webhook", "regression", "marketplace_e2e" })
    public void TC_02_Verify_RefundCreated_Event() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

        String refundId = TestContext.getRefundId();
        String paymentIntentId = TestContext.getPaymentIntentId();

        if (refundId != null) {
            // ── E2E path ──────────────────────────────────────────────────────
            // RefundTests.TC_01 already refunded this PaymentIntent (Step 10).
            // We just verify that Stripe fired the charge.refunded / refund.created
            // event for that action — no new refund needed.
            logger.info("Reusing refund from context → refundId: {}, paymentIntentId: {}",
                    refundId, paymentIntentId);
        } else {
            // ── Standalone / fallback path ────────────────────────────────────
            // No prior refund in context; create our own fresh PI + refund.
            paymentIntentId = PaymentIntentHelper.createFallbackPaymentIntent(true);
            fallbackPaymentIntentIds.add(paymentIntentId);
            logger.info("No refund in context – created fresh PaymentIntent: {}", paymentIntentId);

            Map<String, Object> body = new HashMap<>();
            body.put("amount", 800);
            body.put("reason", "requested_by_customer");
            body.put("payment_intent", paymentIntentId);

            logger.info("Creating refund for PaymentIntent ID: {}", paymentIntentId);
            Response refundResponse = Refunds.createRefund(paymentIntentId, body);
            refundResponse.then().spec(ResponseSpec.OK());
            refundId = refundResponse.jsonPath().getString("id");
            logger.info("Created refund for event verification: {}", refundId);
        }

        // Poll for the event Stripe fired when the refund was created
        String eventId = pollForEvent("charge.refunded", paymentIntentId);
        if (eventId == null) {
            logger.info("charge.refunded event not found yet. Polling for refund.created instead");
            eventId = pollForEvent("refund.created", refundId);
        }

        assertThat("Refund event was not found in the events log", eventId, notNullValue());

        // Validate event details
        logger.info("Retrieving event: {}", eventId);
        Response eventResponse = Events.getEvent(eventId);
        eventResponse.then().spec(ResponseSpec.OK())
                .body("id", equalTo(eventId));
        logger.info("Successfully validated refund event details");
    }

    @Test(groups = { "webhook", "negative", "regression" })
    public void TC_03_Retrieve_Event_By_Id_Negative() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));
        Events.getEvent("evt_invalid12345")
                .then()
                .spec(ResponseSpec.not_found());
        logger.info("Successfully verified invalid event ID retrieval rejection");
    }

    @Test(groups = { "webhook", "negative", "auth", "regression" })
    public void TC_04_ListEvents_InvalidAuth() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));
        Events.getEventsWithCustomAuth("invalid_key_12345", null)
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));
        logger.info("Successfully verified list events invalid auth rejection");
    }

    // ***************CLEANUP*******************\\

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        logger.info("🧹 Starting cleanup for WebhookEventTests...");

        // Delete all fallback customers created during test runs
        if (!fallbackCustomerIds.isEmpty()) {
            logger.info("Deleting {} fallback customer(s)...", fallbackCustomerIds.size());
            for (String id : fallbackCustomerIds) {
                try {
                    Customer.deleteCustomer(id);
                    logger.info("🧹 Deleted fallback customer: {}", id);
                } catch (Exception e) {
                    logger.warn("⚠️ Failed to delete fallback customer {}: {}", id, e.getMessage());
                }
            }
        }

        // PaymentIntents cannot be deleted via the API — log them for reference
        if (!fallbackPaymentIntentIds.isEmpty()) {
            logger.info("ℹ️ {} fallback PaymentIntent(s) were created during the test run (cannot be deleted via API):",
                    fallbackPaymentIntentIds.size());
            for (String id : fallbackPaymentIntentIds) {
                logger.info("   - PaymentIntent ID: {}", id);
            }
        }

        // NOTE: TestContext values (customerId, paymentIntentId, etc.)
        // are intentionally NOT cleared here. Shared context must remain intact for
        // any downstream test class that runs after this one in the same suite.

        logger.info("✅ Cleanup complete for WebhookEventTests.");
    }
}

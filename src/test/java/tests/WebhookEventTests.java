package tests;

import endpoints.Customer;
import endpoints.Events;
import endpoints.Refunds;
import helpers.CustomersHelper;
import helpers.PaymentIntentHelper;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import specification.ResponseSpec;
import testbase.BaseClass;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class WebhookEventTests extends BaseClass {

    private final List<String> customerIds = new ArrayList<>();

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

    @Test(groups = { "webhook", "positive", "regression" })
    public void TC_01_Verify_CustomerCreated_Event() {
        logger.info("Testing customer created event verification");
        String name = CustomersHelper.getName();
        String email = faker.internet().safeEmailAddress();
        logger.info("Creating customer for event verification: {} <{}>", name, email);

        Response createResponse = Customer.createCustomer(name, email, null);
        createResponse.then().spec(ResponseSpec.OK());
        String customerId = createResponse.jsonPath().getString("id");
        customerIds.add(customerId);
        logger.info("Created Customer ID: {}", customerId);

        // Poll for customer.created event
        String eventId = pollForEvent("customer.created", customerId);
        assertThat("Event customer.created was not found in the events log", eventId, notNullValue());

        // Validate the event object details
        logger.info("Retrieving event: {}", eventId);
        Events.getEvent(eventId)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(eventId))
                .body("type", equalTo("customer.created"))
                .body("data.object.id", equalTo(customerId))
                .body("data.object.email", equalTo(email))
                .body("data.object.name", equalTo(name));
        logger.info("Successfully validated customer.created event details");
    }

    @Test(groups = { "webhook", "positive", "regression" })
    public void TC_02_Verify_RefundCreated_Event() {
        logger.info("Testing refund created event verification");
        // Create fallback PaymentIntent to refund against
        String paymentIntentId = PaymentIntentHelper.createFallbackPaymentIntent(true);
        logger.info("Created payment intent for refund event: {}", paymentIntentId);

        Map<String, Object> body = new HashMap<>();
        body.put("amount", 800);
        body.put("reason", "requested_by_customer");
        body.put("payment_intent", paymentIntentId);

        logger.info("Creating refund for PaymentIntent ID: {}", paymentIntentId);
        Response refundResponse = Refunds.createRefund(paymentIntentId, body);
        refundResponse.then().spec(ResponseSpec.OK());
        String refundId = refundResponse.jsonPath().getString("id");
        logger.info("Created refund for event verification: {}", refundId);

        // Poll for refund event - stripe uses charge.refunded or refund.created
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
        logger.info("Testing retrieve event by invalid ID");
        Events.getEvent("evt_invalid12345")
                .then()
                .spec(ResponseSpec.not_found());
        logger.info("Successfully verified invalid event ID retrieval rejection");
    }

    @Test(groups = { "webhook", "negative", "auth", "regression" })
    public void TC_04_ListEvents_InvalidAuth() {
        logger.info("Testing list events with invalid auth");
        Events.getEventsWithCustomAuth("invalid_key_12345", null)
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));
        logger.info("Successfully verified list events invalid auth rejection");
    }

    @AfterMethod
    public void cleanup() {
        logger.info("Running cleanup for WebhookEventTests");
        logger.info("Deleting {} customers", customerIds.size());
        for (String id : customerIds) {
            try {
                logger.info("Deleting customer ID: {}", id);
                Customer.deleteCustomer(id);
            } catch (Exception e) {
                logger.error("Cleanup failed for customer: {}", id, e);
            }
        }
        customerIds.clear();
    }
}

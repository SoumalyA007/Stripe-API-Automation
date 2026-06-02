package helpers;

import endpoints.Disputes;
import endpoints.PaymentIntent;
import specification.ResponseSpec;

import java.util.HashMap;
import java.util.Map;

public class DisputesHelper {

    /**
     * Creates a disputed payment intent using the Stripe special test token "tok_createDispute".
     * Then queries the disputes API to find the dispute ID associated with the resulting charge.
     * @return the associated dispute ID
     */
    public static String createDisputedCharge() {
        Map<String, Object> body = new HashMap<>();
        body.put("amount", 1000);
        body.put("currency", "usd");
        body.put("payment_method", "tok_createDispute");
        body.put("confirm", true);

        // 1. Create and confirm the PaymentIntent
        String chargeId = PaymentIntent.createPaymentIntent(body)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("latest_charge");

        // 2. Query disputes filtering by the charge ID
        Map<String, Object> query = new HashMap<>();
        query.put("charge", chargeId);

        return Disputes.listDisputes(query)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("data[0].id");
    }

    /**
     * Retrieves the dispute ID from TestContext, or creates a fallback dispute.
     * @return the dispute ID
     */
    public static String createFallbackDispute() {
        String disputeId = TestContext.getDisputeId();
        if (disputeId == null) {
            disputeId = createDisputedCharge();
            TestContext.setDisputeId(disputeId);
        }
        return disputeId;
    }
}

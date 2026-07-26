package helpers;

import java.util.HashMap;
import java.util.Map;

import endpoints.Disputes;
import endpoints.PaymentIntent;
import specification.ResponseSpec;
import testbase.BaseClass;

public class DisputesHelper {

    public static String createDisputedCharge() {
        Map<String, Object> body = new HashMap<>();
        body.put("amount", BaseClass.amount / 2);
        body.put("currency", "usd");
        body.put("payment_method", "pm_card_createAutoRepresentmentEligibleDispute");
        body.put("confirm", true);
        body.put("automatic_payment_methods[allow_redirects]", "never");
        body.put("automatic_payment_methods[enabled]", true);

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

    public static String createFallbackDispute() {
        String disputeId = TestContext.getDisputeId();
        if (disputeId == null) {
            disputeId = createDisputedCharge();
            TestContext.setDisputeId(disputeId);
        }
        return disputeId;
    }
}

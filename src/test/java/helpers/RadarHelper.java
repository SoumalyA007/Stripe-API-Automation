package helpers;

import endpoints.PaymentIntent;
import endpoints.Radar;
import specification.ResponseSpec;

import java.util.HashMap;
import java.util.Map;

public class RadarHelper {

    /**
     * Creates a payment intent using the Stripe special test token
     * "tok_createEarlyFraudWarning"
     * which automatically triggers an early fraud warning in Stripe.
     * 
     * @return the created charge ID
     */
    public static String createFraudWarningCharge() {
        Map<String, Object> body = new HashMap<>();
        body.put("amount", testbase.BaseClass.amount);
        body.put("currency", "usd");
        body.put("payment_method", "tok_createEarlyFraudWarning");
        body.put("confirm", true);

        return PaymentIntent.createPaymentIntent(body)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("latest_charge");
    }

    /**
     * Creates a fallback early fraud warning ID by generating a charge and querying
     * its warning.
     * 
     * @return the early fraud warning ID
     */
    public static String createFallbackEarlyFraudWarning() {
        String warningId = TestContext.getEarlyFraudWarningId();
        if (warningId == null) {
            String chargeId = createFraudWarningCharge();

            Map<String, Object> query = new HashMap<>();
            query.put("charge", chargeId);

            warningId = Radar.listEarlyFraudWarnings(query)
                    .then()
                    .spec(ResponseSpec.OK())
                    .extract()
                    .jsonPath()
                    .getString("data[0].id");

            TestContext.setEarlyFraudWarningId(warningId);
        }
        return warningId;
    }
}

package helpers;

import endpoints.Payouts;
import specification.ResponseSpec;

import java.util.HashMap;
import java.util.Map;

public class PayoutsHelper {

    public static String createFallbackPayout() {
        // Charge platform and bypass pending to ensure available balance is funded
        PaymentIntentHelper.createFallbackPaymentIntent(true);

        Map<String, Object> body = new HashMap<>();
        body.put("amount", testbase.BaseClass.amount / 2);
        body.put("currency", "usd");

        return Payouts.createPayout(body)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("id");
    }
}

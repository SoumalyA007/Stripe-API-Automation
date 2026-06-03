package helpers;

import endpoints.Payouts;
import specification.ResponseSpec;

import java.util.HashMap;
import java.util.Map;

public class PayoutsHelper {

    /**
     * Creates a fallback payout on the main account.
     * 
     * @return the created payout ID
     */
    public static String createFallbackPayout() {
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

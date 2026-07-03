package helpers;

import endpoints.PaymentIntent;
import endpoints.Transfers;
import specification.ResponseSpec;

import java.util.HashMap;
import java.util.Map;

public class TransfersHelper {

    /**
     * Creates a fallback transfer to the configured merchant account.
     *
     * <p>
     * Uses {@code source_transaction} so the transfer is funded directly from the
     * charge on a freshly confirmed PaymentIntent — this avoids
     * {@code balance_insufficient}
     * errors regardless of the platform account's available balance.
     *
     * @return the created transfer ID
     */
    public static String createFallbackTransfer() {
        // Create + confirm a fresh PaymentIntent to obtain a real charge
        String paymentIntentId = PaymentIntentHelper.createFallbackPaymentIntent(true);

        io.restassured.path.json.JsonPath piJson = PaymentIntent.retrievePaymentIntent(paymentIntentId)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath();

        int amountPaid = piJson.getInt("amount_received");
        String chargeId = piJson.getString("latest_charge");

        String connectAccountId = testbase.BaseClass.p.getProperty("merchant_account_id");

        Map<String, Object> body = new HashMap<>();
        body.put("amount", amountPaid);
        body.put("currency", "usd");
        body.put("destination", connectAccountId);
        // Fund directly from the charge — no platform balance needed
        if (chargeId != null) {
            body.put("source_transaction", chargeId);
        }

        return Transfers.createTransfer(body)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("id");
    }
}

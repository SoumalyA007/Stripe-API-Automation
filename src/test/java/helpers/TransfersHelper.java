package helpers;

import endpoints.Transfers;
import specification.ResponseSpec;

import java.util.HashMap;
import java.util.Map;

public class TransfersHelper {

    /**
     * Creates a fallback transfer to a freshly created connected account.
     * 
     * @return the created transfer ID
     */
    public static String createFallbackTransfer() {
        String connectAccountId = ConnectedAccountHelper.createConnectAccount(false);

        Map<String, Object> body = new HashMap<>();
        body.put("amount", testbase.BaseClass.amount / 2);
        body.put("currency", "usd");
        body.put("destination", connectAccountId);

        return Transfers.createTransfer(body)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("id");
    }
}

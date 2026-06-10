package helpers;

import java.util.HashMap;
import java.util.Map;

import endpoints.Refunds;
import specification.ResponseSpec;
import testbase.BaseClass;

public class RefundHelper {

    public static String createFallbackRefund() {

        String paymentIntentId = TestContext.getPaymentIntentId();

        if (paymentIntentId == null) {
            paymentIntentId = PaymentIntentHelper.createFallbackPaymentIntent(true);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("amount", BaseClass.amount / 2);
        body.put("currency", "usd");
        body.put("payment_intent", paymentIntentId);

        return Refunds.createRefund(paymentIntentId, body)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("id");
    }

    /**
     * Creates a fresh refund and immediately cancels it.
     * Use this as a prerequisite for negative cancel tests that need
     * an already-cancelled refund (mirrors createCancelledPaymentIntent()).
     *
     * @return the cancelled refund ID
     */
    public static String createCancelledRefund() {
        String refundId = createFallbackRefund();
        Refunds.cancelRefund(refundId)
                .then()
                .spec(ResponseSpec.OK());
        return refundId;
    }

}

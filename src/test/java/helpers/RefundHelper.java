package helpers;

import java.util.HashMap;
import java.util.Map;

import endpoints.Refunds;
import specification.ResponseSpec;
import testbase.BaseClass;

public class RefundHelper extends BaseClass {

    public static String createFallbackRefund() {

        String paymentIntentId = TestContext.getPaymentIntentId();

        if (paymentIntentId == null) {
            paymentIntentId = PaymentIntentHelper.createFallbackPaymentIntent(true);
            TestContext.setPaymentIntentId(paymentIntentId);
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

    public static String createCancelledRefund() {
        String refundId = createCancellableRefund();
        Refunds.cancelRefund(refundId);
        return refundId;
    }

    public static String createCancellableRefund() {
        String paymentIntentId = PaymentIntentHelper.createBankTransferPaymentIntentForCancellableRefund();

        Map<String, Object> body = new HashMap<>();
        body.put("payment_intent", paymentIntentId);
        // No amount param → full refund of the succeeded PI

        return Refunds.createRefund(paymentIntentId, body)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("id");
    }

}

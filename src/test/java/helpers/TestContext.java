package helpers;



public class TestContext {

    public static String getPaymentMethodId() {
        return paymentMethodId;
    }

    public static void setPaymentMethodId(String paymentMethodId) {
        TestContext.paymentMethodId = paymentMethodId;
    }

    public static String getCustomerId() {
        return customerId;
    }

    public static void setCustomerId(String customerId) {
        TestContext.customerId = customerId;
    }

    public static String getPaymentIntentId() {
        return paymentIntentId;
    }

    public static void setPaymentIntentId(String paymentIntentId) {
        TestContext.paymentIntentId = paymentIntentId;
    }

    private static String customerId;
    private static  String paymentMethodId;
    private static  String paymentIntentId;

}

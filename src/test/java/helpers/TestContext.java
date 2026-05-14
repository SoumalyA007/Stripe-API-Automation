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

    public static String getBillingName() {
        return billingName;
    }

    public static void setBillingName(String billingName) {
        TestContext.billingName = billingName;
    }

    public static String getBillingEmail() {
        return billingEmail;
    }

    public static void setBillingEmail(String billingEmail) {
        TestContext.billingEmail = billingEmail;
    }


    private static String customerId;
    private static  String paymentMethodId;
    private static  String paymentIntentId;
    private static String billingName;
    private static String billingEmail;

}

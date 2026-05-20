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


    private static final ThreadLocal<String> customerId = new ThreadLocal<>();
    private static final ThreadLocal<String> paymentMethodId = new ThreadLocal<>();
    private static final ThreadLocal<String> paymentIntentId = new ThreadLocal<>();
    private static final ThreadLocal<String> billingName = new ThreadLocal<>();
    private static final ThreadLocal<String> billingEmail = new ThreadLocal<>();

}

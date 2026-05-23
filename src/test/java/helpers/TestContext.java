package helpers;

import java.util.ArrayList;
import java.util.List;

public class TestContext {

    public static String getPaymentMethodId() {
        return paymentMethodId.get();
    }

    public static void setPaymentMethodId(String paymentMethodId) {
        TestContext.paymentMethodId.set(paymentMethodId);
    }

    public static String getCustomerId() {
        return customerId.get();
    }

    public static void setCustomerId(String customerId) {
        TestContext.customerId.set(customerId);
    }

    public static String getPaymentIntentId() {
        return paymentIntentId.get();
    }

    public static void setPaymentIntentId(String paymentIntentId) {
        TestContext.paymentIntentId.set(paymentIntentId);
    }

    public static String getBillingName() {
        return billingName.get();
    }

    public static void setBillingName(String billingName) {
        TestContext.billingName.set(billingName);
    }

    public static String getBillingEmail() {
        return billingEmail.get();
    }

    public static void setBillingEmail(String billingEmail) {
        TestContext.billingEmail.set(billingEmail);
    }

    public static void setCustomerIdList(String id) {
        customerIdList.get().add(id);
    }

    public static String getCustomerIdList() {
        List<String> list = customerIdList.get();
        if (list == null || list.isEmpty()) {
            return null; // or throw new NoSuchElementException("Customer list is empty");
        }
        return list.get(list.size() - 1);
    }

    private static final ThreadLocal<String> customerId = new ThreadLocal<>();
    private static final ThreadLocal<String> paymentMethodId = new ThreadLocal<>();
    private static final ThreadLocal<String> paymentIntentId = new ThreadLocal<>();
    private static final ThreadLocal<String> billingName = new ThreadLocal<>();
    private static final ThreadLocal<String> billingEmail = new ThreadLocal<>();
    private static final ThreadLocal<List<String>> customerIdList = ThreadLocal.withInitial(ArrayList::new);

}
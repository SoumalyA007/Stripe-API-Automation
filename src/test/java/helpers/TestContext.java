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

    public static void setChargeId(String chargeId) {
        TestContext.chargeId.set(chargeId);
    }

    public static String getChargeId() {
        return chargeId.get();
    }

    public static void setRefundId(String refundId) {
        TestContext.refundId.set(refundId);
    }

    public static String getRefundId() {
        return refundId.get();
    }

    public static void setCanceledRefundId(String canceledRefundId) {
        TestContext.canceledRefundId.set(canceledRefundId);
    }

    public static String getCanceledRefundId() {
        return canceledRefundId.get();
    }

    public static void setSetupIntentId(String setupIntentId) {
        TestContext.setupIntentId.set(setupIntentId);
    }

    public static String getSetupIntentId() {
        return setupIntentId.get();
    }

    public static void setSubscriptionId(String subscriptionId) {
        TestContext.subscriptionId.set(subscriptionId);
    }

    public static String getSubscriptionId() {
        return subscriptionId.get();
    }

    public static void setProductId(String productId) {
        TestContext.productId.set(productId);
    }

    public static String getProductId() {
        return productId.get();
    }

    public static void setPriceId(String priceId) {
        TestContext.priceId.set(priceId);
    }

    public static String getPriceId() {
        return priceId.get();
    }

    public static void setConnectAccountId(String connectAccountId) {
        TestContext.connectAccountId.set(connectAccountId);
    }

    public static String getConnectAccountId() {
        return connectAccountId.get();
    }

    public static void setTransferId(String transferId) {
        TestContext.transferId.set(transferId);
    }

    public static String getTransferId() {
        return transferId.get();
    }

    public static void setPayoutId(String payoutId) {
        TestContext.payoutId.set(payoutId);
    }

    public static String getPayoutId() {
        return payoutId.get();
    }

    public static void setInvoiceId(String invoiceId) {
        TestContext.invoiceId.set(invoiceId);
    }

    public static String getInvoiceId() {
        return invoiceId.get();
    }

    public static void setDisputeId(String disputeId) {
        TestContext.disputeId.set(disputeId);
    }

    public static String getDisputeId() {
        return disputeId.get();
    }

    public static void setEarlyFraudWarningId(String earlyFraudWarningId) {
        TestContext.earlyFraudWarningId.set(earlyFraudWarningId);
    }

    public static String getEarlyFraudWarningId() {
        return earlyFraudWarningId.get();
    }

    public static String getConfirmPaymentIntent() {
        return confirmPaymentIntent.get();
    }

    public static void setConfirmPaymentIntent(String confirmPaymentIntentId) {
        TestContext.confirmPaymentIntent.set(confirmPaymentIntentId);
    }

    public static String getCanceledPaymentIntent() {
        return canceledPaymentIntent.get();
    }

    public static void setCanceledPaymentIntent(String canceledPaymentIntentId) {
        TestContext.canceledPaymentIntent.set(canceledPaymentIntentId);
    }

    public static String getConfirmedSetupIntentId() {
        return confirmedSetupIntentId.get();
    }

    public static void setConfirmedSetupIntentId(String confirmedSetupIntentId) {
        TestContext.confirmedSetupIntentId.set(confirmedSetupIntentId);
    }

    public static String getSubscriptionCustomerId() {
        return subscriptionCustomerId.get();
    }

    public static void setSubscriptionCustomerId(String subscriptionCustomerId) {
        TestContext.subscriptionCustomerId.set(subscriptionCustomerId);
    }

    public static String getSubscriptionPaymentMethodId() {
        return subscriptionPaymentMethodId.get();
    }

    public static void setSubscriptionPaymentMethodId(String subscriptionPaymentMethodId) {
        TestContext.subscriptionPaymentMethodId.set(subscriptionPaymentMethodId);
    }

    public static String getCancelledSubscriptionId() {
        return cancelledSubscriptionId.get();
    }

    public static void setCancelledSubscriptionId(String cancelledSubscriptionId) {
        TestContext.cancelledSubscriptionId.set(cancelledSubscriptionId);
    }

    public static String getServiceProviderEmail() {
        return serviceProviderEmail.get();
    }

    public static void setServiceProviderEmail(String serviceProviderEmail) {
        TestContext.serviceProviderEmail.set(serviceProviderEmail);
    }

    public static String getAccountId() {
        return accountId.get();
    }

    public static void setAccountId(String accountId) {
        TestContext.accountId.set(accountId);
    }

    public static void clear() {
        customerId.remove();
        paymentMethodId.remove();
        paymentIntentId.remove();
        billingName.remove();
        billingEmail.remove();
        customerIdList.get().clear();
        chargeId.remove();
        refundId.remove();
        canceledRefundId.remove();
        setupIntentId.remove();
        subscriptionId.remove();
        productId.remove();
        priceId.remove();
        connectAccountId.remove();
        transferId.remove();
        payoutId.remove();
        invoiceId.remove();
        disputeId.remove();
        earlyFraudWarningId.remove();
        confirmPaymentIntent.remove();
        canceledPaymentIntent.remove();
        confirmedSetupIntentId.remove();
        subscriptionCustomerId.remove();
        subscriptionPaymentMethodId.remove();
        cancelledSubscriptionId.remove();
        serviceProviderEmail.remove();
        accountId.remove();
    }

    private static final ThreadLocal<String> customerId = new ThreadLocal<>();
    private static final ThreadLocal<String> paymentMethodId = new ThreadLocal<>();
    private static final ThreadLocal<String> paymentIntentId = new ThreadLocal<>();
    private static final ThreadLocal<String> billingName = new ThreadLocal<>();
    private static final ThreadLocal<String> billingEmail = new ThreadLocal<>();
    private static final ThreadLocal<List<String>> customerIdList = ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<String> chargeId = new ThreadLocal<>();
    private static final ThreadLocal<String> refundId = new ThreadLocal<>();
    private static final ThreadLocal<String> canceledRefundId = new ThreadLocal<>();
    private static final ThreadLocal<String> setupIntentId = new ThreadLocal<>();
    private static final ThreadLocal<String> subscriptionId = new ThreadLocal<>();
    private static final ThreadLocal<String> productId = new ThreadLocal<>();
    private static final ThreadLocal<String> priceId = new ThreadLocal<>();
    private static final ThreadLocal<String> connectAccountId = new ThreadLocal<>();
    private static final ThreadLocal<String> transferId = new ThreadLocal<>();
    private static final ThreadLocal<String> payoutId = new ThreadLocal<>();
    private static final ThreadLocal<String> invoiceId = new ThreadLocal<>();
    private static final ThreadLocal<String> disputeId = new ThreadLocal<>();
    private static final ThreadLocal<String> earlyFraudWarningId = new ThreadLocal<>();
    private static final ThreadLocal<String> confirmPaymentIntent = new ThreadLocal<>();
    private static final ThreadLocal<String> canceledPaymentIntent = new ThreadLocal<>();
    private static final ThreadLocal<String> confirmedSetupIntentId = new ThreadLocal<>();
    private static final ThreadLocal<String> subscriptionCustomerId = new ThreadLocal<>();
    private static final ThreadLocal<String> subscriptionPaymentMethodId = new ThreadLocal<>();
    private static final ThreadLocal<String> cancelledSubscriptionId = new ThreadLocal<>();
    private static final ThreadLocal<String> serviceProviderEmail = new ThreadLocal<>();
    private static final ThreadLocal<String> accountId = new ThreadLocal<>();

}
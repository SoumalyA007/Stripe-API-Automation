package listeners;

import endpoints.*;
import helpers.TestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import java.util.HashMap;

public class SuiteCleanupListener implements ISuiteListener {

    private static final Logger log = LogManager.getLogger(SuiteCleanupListener.class);

    @Override
    public void onStart(ISuite suite) {
        // nothing needed before the suite starts
    }

    @Override
    public void onFinish(ISuite suite) {
        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║  SuiteCleanupListener – starting post-suite cleanup      ║");
        log.info("╚══════════════════════════════════════════════════════════╝");

        // ── 1. Payment Intents ────────────────────────────────────────────────
        cancelPaymentIntent("paymentIntentId", TestContext.getPaymentIntentId());
        cancelPaymentIntent("confirmPaymentIntent", TestContext.getConfirmPaymentIntent());
        // canceledPaymentIntent is already terminal – skip API call, just clear

        // ── 2. Setup Intents ─────────────────────────────────────────────────
        cancelSetupIntent("setupIntentId", TestContext.getSetupIntentId());
        // confirmedSetupIntentId is already succeeded (terminal) – skip API call

        // ── 3. Subscriptions ─────────────────────────────────────────────────
        cancelSubscription("subscriptionId", TestContext.getSubscriptionId());
        // cancelledSubscriptionId is already cancelled – skip API call

        // ── 4. Transfers – must be reversed (cannot be deleted) ───────────────
        reverseTransfer("transferId", TestContext.getTransferId());

        // ── 5. Payouts – cancel only while still in 'pending' state ──────────
        cancelPayout("payoutId", TestContext.getPayoutId());

        // ── 6. Connected Accounts ─────────────────────────────────────────────
        deleteConnectAccount("connectAccountId", TestContext.getConnectAccountId());

        // ── 7. Products ───────────────────────────────────────────────────────
        // Must cancel active subscription first (done above) so the product
        // has no active price attached before deletion.
        deleteProduct("productId", TestContext.getProductId());

        // ── 8. Customers (individual fields) ─────────────────────────────────
        deleteCustomer("customerId", TestContext.getCustomerId());
        deleteCustomer("subscriptionCustomerId", TestContext.getSubscriptionCustomerId());

        // ── 9. customerIdList (batch) ─────────────────────────────────────────
        deleteCustomerList();

        // ── 10. Payment Methods – detach ──────────────────────────────────────
        detachPaymentMethod("paymentMethodId", TestContext.getPaymentMethodId());
        detachPaymentMethod("subscriptionPaymentMethodId", TestContext.getSubscriptionPaymentMethodId());

        // ── 11. V2 Accounts (accounts.java / /v2/core/accounts) ──────────────
        closeAccount("accountId", TestContext.getAccountId());

        log.info("ℹ️  Skipping API calls for read-only/non-deletable context fields " +
                "(refundId, chargeId, priceId, disputeId, earlyFraudWarningId, billing*, email)");

        // ── 13. Clear all TestContext ThreadLocals ────────────────────────────
        TestContext.clear();
        log.info("✅ TestContext cleared.");
        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║  SuiteCleanupListener – cleanup complete                 ║");
        log.info("╚══════════════════════════════════════════════════════════╝");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers – each one is fully guarded so a single failure never
    // prevents the remaining resources from being cleaned up.
    // ─────────────────────────────────────────────────────────────────────────

    private void cancelPaymentIntent(String label, String id) {
        if (id == null)
            return;
        log.info("🧹 Cancelling PaymentIntent [{}]: {}", label, id);
        try {
            int status = PaymentIntent.cancelPaymentIntent(id, new HashMap<>()).getStatusCode();
            if (status == 200) {
                log.info("   ✅ Cancelled PaymentIntent: {}", id);
            } else {
                // 400 payment_intent_unexpected_state = already succeeded/refunded/cancelled
                log.warn("   ⚠️  Cancel returned HTTP {} for PaymentIntent {} – may already be terminal", status, id);
            }
        } catch (Exception e) {
            log.error("   ❌ Failed to cancel PaymentIntent {}: {}", id, e.getMessage());
        }
    }

    private void cancelSetupIntent(String label, String id) {
        if (id == null)
            return;
        log.info("🧹 Cancelling SetupIntent [{}]: {}", label, id);
        try {
            int status = SetupIntent.cancelSetupIntent(id).getStatusCode();
            if (status == 200) {
                log.info("   ✅ Cancelled SetupIntent: {}", id);
            } else {
                log.warn("   ⚠️  Cancel returned HTTP {} for SetupIntent {} – may already be terminal", status, id);
            }
        } catch (Exception e) {
            log.error("   ❌ Failed to cancel SetupIntent {}: {}", id, e.getMessage());
        }
    }

    private void cancelSubscription(String label, String id) {
        if (id == null)
            return;
        log.info("🧹 Cancelling Subscription [{}]: {}", label, id);
        try {
            int status = Subscription.cancelSubscription(id).getStatusCode();
            if (status == 200) {
                log.info("   ✅ Cancelled Subscription: {}", id);
            } else {
                log.warn("   ⚠️  Cancel returned HTTP {} for Subscription {} – may already be terminal", status, id);
            }
        } catch (Exception e) {
            log.error("   ❌ Failed to cancel Subscription {}: {}", id, e.getMessage());
        }
    }

    private void reverseTransfer(String label, String id) {
        if (id == null)
            return;
        log.info("🧹 Reversing Transfer [{}]: {}", label, id);
        try {
            // Empty body = reverse the full remaining amount
            int status = Transfers.reverseTransfer(id, new HashMap<>()).getStatusCode();
            if (status == 200) {
                log.info("   ✅ Reversed Transfer: {}", id);
            } else {
                log.warn("   ⚠️  Reverse returned HTTP {} for Transfer {} – may already be fully reversed", status, id);
            }
        } catch (Exception e) {
            log.error("   ❌ Failed to reverse Transfer {}: {}", id, e.getMessage());
        }
    }

    private void cancelPayout(String label, String id) {
        if (id == null)
            return;
        log.info("🧹 Cancelling Payout [{}]: {}", label, id);
        try {
            int status = Payouts.cancelPayout(id).getStatusCode();
            if (status == 200) {
                log.info("   ✅ Cancelled Payout: {}", id);
            } else {
                // 400 = not in 'pending' state – can't cancel paid/failed payouts
                log.warn("   ⚠️  Cancel returned HTTP {} for Payout {} – may not be in 'pending' state", status, id);
            }
        } catch (Exception e) {
            log.error("   ❌ Failed to cancel Payout {}: {}", id, e.getMessage());
        }
    }

    private void deleteConnectAccount(String label, String id) {
        if (id == null)
            return;
        log.info("🧹 Deleting ConnectAccount [{}]: {}", label, id);
        try {
            int status = ConnectAccounts.deleteConnectAccount(id).getStatusCode();
            if (status == 200) {
                log.info("   ✅ Deleted ConnectAccount: {}", id);
            } else {
                log.warn("   ⚠️  Delete returned HTTP {} for ConnectAccount {}", status, id);
            }
        } catch (Exception e) {
            log.error("   ❌ Failed to delete ConnectAccount {}: {}", id, e.getMessage());
        }
    }

    private void deleteProduct(String label, String id) {
        if (id == null)
            return;
        log.info("🧹 Deleting Product [{}]: {}", label, id);
        try {
            int status = Product.deleteProduct(id).getStatusCode();
            if (status == 200) {
                log.info("   ✅ Deleted Product: {}", id);
            } else {
                log.warn("   ⚠️  Delete returned HTTP {} for Product {}", status, id);
            }
        } catch (Exception e) {
            log.error("   ❌ Failed to delete Product {}: {}", id, e.getMessage());
        }
    }

    private void deleteCustomer(String label, String id) {
        if (id == null)
            return;
        log.info("🧹 Deleting Customer [{}]: {}", label, id);
        try {
            int status = Customer.deleteCustomer(id).getStatusCode();
            if (status == 200) {
                log.info("   ✅ Deleted Customer: {}", id);
            } else {
                log.warn("   ⚠️  Delete returned HTTP {} for Customer {}", status, id);
            }
        } catch (Exception e) {
            log.error("   ❌ Failed to delete Customer {}: {}", id, e.getMessage());
        }
    }

    private void deleteCustomerList() {
        String id;
        int count = 0;
        while ((id = TestContext.getCustomerIdList()) != null) {
            deleteCustomer("customerIdList[" + count + "]", id);
            count++;
            // Safety: if the list keeps returning the same non-null value, break
            if (count > 500) {
                log.warn("   ⚠️  customerIdList exceeded 500 entries – breaking to avoid infinite loop");
                break;
            }
        }
        if (count > 0) {
            log.info("   ℹ️  Processed {} customer(s) from customerIdList", count);
        }
    }

    private void detachPaymentMethod(String label, String id) {
        if (id == null)
            return;
        log.info("🧹 Detaching PaymentMethod [{}]: {}", label, id);
        try {
            int status = paymentMethods.detachPaymentMethod(id).getStatusCode();
            if (status == 200) {
                log.info("   ✅ Detached PaymentMethod: {}", id);
            } else {
                log.warn("   ⚠️  Detach returned HTTP {} for PaymentMethod {}", status, id);
            }
        } catch (Exception e) {
            log.error("   ❌ Failed to detach PaymentMethod {}: {}", id, e.getMessage());
        }
    }

    private void closeAccount(String label, String id) {
        if (id == null)
            return;
        log.info("🧹 Closing V2 Account [{}]: {}", label, id);
        try {
            int status = accounts.closeAccount(id).getStatusCode();
            if (status == 200) {
                log.info("   ✅ Closed V2 Account: {}", id);
            } else {
                log.warn("   ⚠️  Close returned HTTP {} for V2 Account {}", status, id);
            }
        } catch (Exception e) {
            log.error("   ❌ Failed to close V2 Account {}: {}", id, e.getMessage());
        }
    }
}

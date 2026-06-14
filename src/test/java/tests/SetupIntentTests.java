package tests;

import dataprovider.SetupIntentDataProvider;
import endpoints.Customer;
import endpoints.PaymentIntent;
import endpoints.SetupIntent;
import endpoints.paymentMethods;
import helpers.CustomersHelper;
import helpers.PaymentMethodsHelper;
import helpers.SetupIntentHelper;
import helpers.TestContext;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import specification.ResponseSpec;
import testbase.BaseClass;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SetupIntentTests extends BaseClass {

        List<String> customerIdsToCleanup = new ArrayList<>();
        List<String> setupIntentIdsToCleanup = new ArrayList<>();
        List<String> paymentMethodIdsToCleanup = new ArrayList<>();
        static String confirmedIntentId = null;

        // ═══════════════════════════════════════════════════════════════
        // ██ SETUP INTENT — CREATE
        // ═══════════════════════════════════════════════════════════════

        @Test(groups = { "setup_intent", "regression" })
        public void TC_01_positive_Create_Setup_Intent() {
                logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

                // Ensure customer exists
                String customerId = TestContext.getCustomerId();
                if (customerId == null) {
                        customerId = SetupIntentHelper.createFallbackCustomer();
                        TestContext.setCustomerId(customerId);

                }

                Map<String, Object> body = new HashMap<>();
                body.put("customer", customerId);
                body.put("usage", "off_session");
                body.put("payment_method_types[0]", "card");

                Response resp = SetupIntent.createSetupIntent(body);
                String setupIntentId = resp.then()
                                .spec(ResponseSpec.OK())
                                .body("id", notNullValue())
                                .body("object", equalTo("setup_intent"))
                                .body("status", equalTo("requires_payment_method"))
                                .body("usage", equalTo("off_session"))
                                .body("customer", equalTo(customerId))
                                .extract()
                                .jsonPath()
                                .getString("id");

                TestContext.setSetupIntentId(setupIntentId);

                logger.info("✅ SetupIntent created: {}", setupIntentId);
        }

        @Test(groups = { "setup_intent", "regression" })
        public void TC_02_positive_Create_Setup_Intent_Without_Customer() {
                logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

                Map<String, Object> body = new HashMap<>();
                body.put("usage", "off_session");
                body.put("payment_method_types[0]", "card");

                Response resp = SetupIntent.createSetupIntent(body);
                String setupIntentId = resp.then()
                                .spec(ResponseSpec.OK())
                                .body("id", notNullValue())
                                .body("object", equalTo("setup_intent"))
                                .body("status", equalTo("requires_payment_method"))
                                .body("customer", nullValue())
                                .extract()
                                .jsonPath()
                                .getString("id");

                setupIntentIdsToCleanup.add(setupIntentId);
                logger.info("✅ SetupIntent (no customer) created: {}", setupIntentId);
        }

        @Test(groups = { "setup_intent", "regression" })
        public void TC_03_positive_Create_Setup_Intent_OnSession() {
                logger.info("Testing create setup intent on_session");
                Map<String, Object> body = new HashMap<>();
                body.put("usage", "on_session");
                body.put("payment_method_types[0]", "card");

                Response resp = SetupIntent.createSetupIntent(body);
                String setupIntentId = resp.then()
                                .spec(ResponseSpec.OK())
                                .body("usage", equalTo("on_session"))
                                .extract()
                                .jsonPath()
                                .getString("id");

                setupIntentIdsToCleanup.add(setupIntentId);
                logger.info("✅ SetupIntent (on_session) created: {}", setupIntentId);
        }

        // ═══════════════════════════════════════════════════════════════
        // ██ SETUP INTENT — CONFIRM
        // ═══════════════════════════════════════════════════════════════

        // confirming setup intent
        @Test(groups = { "setup_intent", "regression" })
        public void TC_04_positive_Confirm_Setup_Intent() {
                logger.info("Test running under groups: {}", Arrays.toString(currentGroups));
                logger.info("Testing positive confirmation of SetupIntent");

                String setupIntentId = TestContext.getSetupIntentId();
                if (setupIntentId == null) {
                        setupIntentId = SetupIntentHelper.createSetupIntent(true);
                        TestContext.setSetupIntentId(setupIntentId);
                        logger.info("Created fallback SetupIntent ID: {}", setupIntentId);
                } else {
                        logger.info("Using active SetupIntent ID from context: {}", setupIntentId);
                }

                // Create and use a valid payment method for confirmation
                String paymentMethodId = TestContext.getPaymentMethodId();
                if (paymentMethodId == null) {
                        paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(true);
                }
                logger.info("Created valid payment method ID: {}", paymentMethodId);

                Map<String, Object> confirmBody = new HashMap<>();
                confirmBody.put("payment_method", paymentMethodId);

                logger.info("Confirming SetupIntent ID: {}", setupIntentId);
                SetupIntent.confirmSetupIntent(setupIntentId, confirmBody)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("status", equalTo("succeeded"))
                                .body("payment_method", equalTo(paymentMethodId))
                                .body("object", equalTo("setup_intent"));

                logger.info("✅ SetupIntent confirmed: {}", setupIntentId);
        }

        // confirm setup intent using declined card
        @Test(groups = { "setup_intent", "negative",
                        "regression" }, dataProvider = "declinedCardTokensForSetupIntent", dataProviderClass = SetupIntentDataProvider.class)
        public void TC_05_negative_Confirm_Setup_Intent_Declined_Card(String testCaseName,
                        String cardToken, String expectedErrorCode) {

                logger.info("Running declined card scenario: {}", testCaseName);

                // Create a setup intent
                Map<String, Object> siBody = new HashMap<>();
                siBody.put("usage", "off_session");
                siBody.put("payment_method_types[0]", "card");

                String setupIntentId = SetupIntent.createSetupIntent(siBody)
                                .then()
                                .spec(ResponseSpec.OK())
                                .extract()
                                .jsonPath()
                                .getString("id");
                logger.info("Created SetupIntent ID: {}", setupIntentId);

                setupIntentIdsToCleanup.add(setupIntentId);

                // Create a payment method with the declined card token
                Map<String, Object> pmBody = new HashMap<>();
                pmBody.put("type", "card");
                pmBody.put("card[token]", cardToken);
                String declinedPmId = paymentMethods.createPaymentMethod(pmBody)
                                .then()
                                .extract()
                                .jsonPath()
                                .getString("id");
                paymentMethodIdsToCleanup.add(declinedPmId);
                logger.info("Created payment method ID: {} with token {}", declinedPmId, cardToken);

                // Attempt to confirm — should fail
                Map<String, Object> confirmBody = new HashMap<>();
                confirmBody.put("payment_method", declinedPmId);

                logger.info("Attempting to confirm SetupIntent with declined payment method");
                SetupIntent.confirmSetupIntent(setupIntentId, confirmBody)
                                .then()
                                .spec(ResponseSpec.request_failed())
                                .body("error.code", containsString(expectedErrorCode));

                logger.info("✅ Correctly declined: {} → {}", testCaseName, expectedErrorCode);
        }

        // confirming already confirmed setupintnet
        @Test(groups = { "setup_intent", "negative", "regression" })
        public void TC_06_negative_Confirm_Already_Succeeded_Setup_Intent() {
                logger.info("Testing confirm already succeeded SetupIntent");
                // Create and confirm a SetupIntent first
                String confirmedPaymentIntentId = TestContext.getConfirmedSetupIntentId();
                if (confirmedPaymentIntentId == null) {
                        String setupIntentId = SetupIntentHelper.createAndConfirmSetupIntent();
                        logger.info("Created and confirmed SetupIntent ID: {}", setupIntentId);
                        TestContext.setConfirmedSetupIntentId(setupIntentId);
                } else {
                        logger.info("Using active SetupIntent ID from context: {}", confirmedPaymentIntentId);
                }

                String paymentMethodId = TestContext.getPaymentMethodId();
                if (paymentMethodId == null) {
                        paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(false);
                        logger.info("Created fallback payment method ID: {}", paymentMethodId);
                        TestContext.setPaymentMethodId(paymentMethodId);
                }

                Map<String, Object> confirmBody = new HashMap<>();
                confirmBody.put("payment_method", paymentMethodId);

                // Attempting to confirm an already succeeded SetupIntent
                logger.info("Attempting to confirm already succeeded SetupIntent ID: {}", confirmedPaymentIntentId);
                SetupIntent.confirmSetupIntent(confirmedPaymentIntentId, confirmBody)
                                .then()
                                .spec(ResponseSpec.request_failed())
                                .body("error.code", equalTo("setup_intent_unexpected_state"));

                logger.info("✅ Correctly rejected re-confirmation of succeeded SetupIntent");
        }

        // confirming already cancelled setupintnet
        @Test(groups = { "setup_intent", "negative", "regression" })
        public void TC_07_negative_Confirm_Canceled_Setup_Intent() {
                logger.info("Testing confirm canceled SetupIntent");
                // Create a SetupIntent and cancel it
                Map<String, Object> siBody = new HashMap<>();
                siBody.put("usage", "off_session");
                siBody.put("payment_method_types[0]", "card");

                String setupIntentId = SetupIntent.createSetupIntent(siBody)
                                .then()
                                .spec(ResponseSpec.OK())
                                .extract()
                                .jsonPath()
                                .getString("id");
                logger.info("Created SetupIntent ID: {}", setupIntentId);

                // Cancel it
                logger.info("Canceling SetupIntent ID: {}", setupIntentId);
                SetupIntent.cancelSetupIntent(setupIntentId)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("status", equalTo("canceled"));
                logger.info("SetupIntent canceled successfully");

                // Attempt to confirm a canceled SetupIntent
                String paymentMethodId = TestContext.getPaymentMethodId();
                if (paymentMethodId == null) {
                        paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(false);
                        logger.info("Created fallback payment method ID: {}", paymentMethodId);
                        TestContext.setPaymentMethodId(paymentMethodId);
                }
                Map<String, Object> confirmBody = new HashMap<>();
                confirmBody.put("payment_method", paymentMethodId);

                logger.info("Attempting to confirm canceled SetupIntent ID: {}", setupIntentId);
                SetupIntent.confirmSetupIntent(setupIntentId, confirmBody)
                                .then()
                                .spec(ResponseSpec.request_failed())
                                .body("error.code", equalTo("setup_intent_unexpected_state"));

                logger.info("✅ Correctly rejected confirmation of canceled SetupIntent");
        }

        // ═══════════════════════════════════════════════════════════════
        // ██ SETUP INTENT — RETRIEVE
        // ═══════════════════════════════════════════════════════════════

        @Test(groups = { "setup_intent", "regression" })
        public void TC_08_positive_Retrieve_Setup_Intent() {
                logger.info("Testing retrieve SetupIntent");
                String setupIntentId = TestContext.getSetupIntentId();
                if (setupIntentId == null) {
                        setupIntentId = SetupIntentHelper.createSetupIntent(false);
                        TestContext.setSetupIntentId(setupIntentId);
                }
                logger.info("Created SetupIntent ID: {}", setupIntentId);

                logger.info("Retrieving SetupIntent ID: {}", setupIntentId);
                SetupIntent.retrieveSetupIntent(setupIntentId)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("id", equalTo(setupIntentId))
                                .body("object", equalTo("setup_intent"));

                logger.info("✅ SetupIntent retrieved: {}", setupIntentId);
        }

        @Test(groups = { "setup_intent", "negative",
                        "regression" }, dataProvider = "invalidSetupIntentIds", dataProviderClass = SetupIntentDataProvider.class)
        public void TC_09_negative_Retrieve_Invalid_Setup_Intent(String invalidId,
                        io.restassured.specification.ResponseSpecification expectedSpec) {
                logger.info("Testing retrieve invalid SetupIntent ID: {}", invalidId);

                SetupIntent.retrieveSetupIntent(invalidId)
                                .then()
                                .spec(expectedSpec);

                logger.info("✅ Correctly failed retrieval for invalid ID: {}", invalidId);
        }

        // ═══════════════════════════════════════════════════════════════
        // ██ SETUP INTENT — UPDATE
        // ═══════════════════════════════════════════════════════════════

        @Test(groups = { "setup_intent",
                        "regression" }, dataProvider = "setupIntentMetadataUpdates", dataProviderClass = SetupIntentDataProvider.class)
        public void TC_10_positive_Update_Setup_Intent_Metadata(String key, String value) {
                logger.info("Testing update SetupIntent metadata: {}={}", key, value);
                String setupIntentId = TestContext.getSetupIntentId();
                if (setupIntentId == null) {
                        setupIntentId = SetupIntentHelper.createSetupIntent(false);
                        TestContext.setSetupIntentId(setupIntentId);
                        logger.info("Created fallback SetupIntent ID: {}", setupIntentId);
                } else {
                        logger.info("Using SetupIntent ID from context: {}", setupIntentId);
                }

                Map<String, Object> updateBody = new HashMap<>();
                updateBody.put("metadata[" + key + "]", value);

                logger.info("Updating SetupIntent ID: {}", setupIntentId);
                SetupIntent.updateSetupIntent(setupIntentId, updateBody)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("metadata." + key, equalTo(value));

                logger.info("✅ SetupIntent metadata updated: {}={}", key, value);
        }

        @Test(groups = { "setup_intent", "negative", "regression" })
        public void TC_11_negative_Update_Nonexistent_Setup_Intent() {
                logger.info("Testing update nonexistent SetupIntent");
                Map<String, Object> updateBody = new HashMap<>();
                updateBody.put("metadata[key]", "value");

                SetupIntent.updateSetupIntent("seti_nonexistent_id", updateBody)
                                .then()
                                .spec(ResponseSpec.not_found())
                                .body("error.type", equalTo("invalid_request_error"));

                logger.info("✅ Correctly failed update for nonexistent SetupIntent");
        }

        // ═══════════════════════════════════════════════════════════════
        // ██ SETUP INTENT — CANCEL
        // ═══════════════════════════════════════════════════════════════

        @Test(groups = { "setup_intent",
                        "regression" }, dataProvider = "cancellationReasons", dataProviderClass = SetupIntentDataProvider.class)
        public void TC_12_positive_Cancel_Setup_Intent(String reason) {
                logger.info("Testing cancel SetupIntent with reason: {}", reason);
                // Each cancellation iteration needs a fresh SI (a canceled SI cannot be
                // reused),
                // so we always create one here and do NOT store it in TestContext.
                String setupIntentId = SetupIntentHelper.createSetupIntent(false);
                setupIntentIdsToCleanup.add(setupIntentId);
                logger.info("Created SetupIntent ID: {}", setupIntentId);

                Map<String, Object> cancelBody = new HashMap<>();
                cancelBody.put("cancellation_reason", reason);

                logger.info("Canceling SetupIntent ID: {}", setupIntentId);
                SetupIntent.cancelSetupIntent(setupIntentId, cancelBody)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("status", equalTo("canceled"))
                                .body("cancellation_reason", equalTo(reason));

                logger.info("✅ SetupIntent canceled with reason: {}", reason);
        }

        @Test(groups = { "setup_intent", "negative", "regression" })
        public void TC_13_negative_Cancel_Already_Canceled_Setup_Intent() {
                logger.info("Testing cancel already canceled SetupIntent");
                // Needs a fresh SI — a canceled SI from context would already be terminal.
                String setupIntentId = SetupIntentHelper.createSetupIntent(false);
                setupIntentIdsToCleanup.add(setupIntentId);
                logger.info("Created SetupIntent ID: {}", setupIntentId);

                // First cancel succeeds
                logger.info("Performing first cancellation on ID: {}", setupIntentId);
                SetupIntent.cancelSetupIntent(setupIntentId)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("status", equalTo("canceled"));
                logger.info("First cancellation succeeded");

                // Second cancel fails
                logger.info("Performing second cancellation on ID: {}", setupIntentId);
                SetupIntent.cancelSetupIntent(setupIntentId)
                                .then()
                                .spec(ResponseSpec.request_failed())
                                .body("error.code", equalTo("setup_intent_unexpected_state"));

                logger.info("✅ Correctly rejected double-cancellation of SetupIntent");
        }

        @Test(groups = { "setup_intent", "negative", "regression" })
        public void TC_14_negative_Cancel_Succeeded_Setup_Intent() {
                logger.info("Testing cancel already succeeded SetupIntent");
                // Needs a confirmed (succeeded) SI — always create fresh so state is
                // guaranteed.
                String confirmSetupIntentId = TestContext.getConfirmedSetupIntentId();
                if (confirmSetupIntentId == null) {
                        String setupIntentId = SetupIntentHelper.createAndConfirmSetupIntent();
                        TestContext.setConfirmedSetupIntentId(setupIntentId);

                }
                logger.info("Created and confirmed SetupIntent ID: {}", confirmSetupIntentId);

                logger.info("Attempting to cancel succeeded SetupIntent ID: {}", confirmSetupIntentId);
                SetupIntent.cancelSetupIntent(confirmSetupIntentId)
                                .then()
                                .spec(ResponseSpec.request_failed())
                                .body("error.code", equalTo("setup_intent_unexpected_state"));

                logger.info("✅ Correctly rejected cancellation of succeeded SetupIntent");
        }

        // ═══════════════════════════════════════════════════════════════
        // ██ SAVED CARD + FUTURE PAYMENT — E2E FLOW
        // ═══════════════════════════════════════════════════════════════

        @Test(groups = { "setup_intent", "regression" })
        public void TC_15_flow_Saved_Card_Future_OffSession_Payment() {
                logger.info("Test running under groups: {}", Arrays.toString(currentGroups));
                logger.info("🔄 Starting E2E: SetupIntent → Saved Card → Off-Session Payment");

                // 1️⃣ Customer — reuse from context if available
                String customerId = TestContext.getCustomerId();
                String name;
                String email;
                if (customerId == null) {
                        name = CustomersHelper.getName();
                        email = faker.internet().safeEmailAddress();
                        Response custResp = Customer.createCustomer(name, email, null);
                        customerId = custResp.jsonPath().getString("id");
                        customerIdsToCleanup.add(customerId);
                        TestContext.setCustomerId(customerId);
                        TestContext.setBillingName(name);
                        TestContext.setBillingEmail(email);
                        logger.info("  Step 1: Customer created → {}", customerId);
                } else {
                        name = TestContext.getBillingName();
                        email = TestContext.getBillingEmail();
                        logger.info("  Step 1: Reusing customer from context → {}", customerId);
                }

                // 2️⃣ Payment method — reuse from context if available, otherwise create +
                // attach
                String paymentMethodId = TestContext.getPaymentMethodId();
                if (paymentMethodId == null) {
                        Map<String, Object> pmBody = new HashMap<>();
                        pmBody.put("type", "card");
                        pmBody.put("card[token]", "tok_visa");
                        pmBody.put("billing_details[email]", email);
                        pmBody.put("billing_details[name]", name);
                        paymentMethodId = paymentMethods.createPaymentMethod(pmBody)
                                        .then()
                                        .spec(ResponseSpec.OK())
                                        .extract()
                                        .jsonPath()
                                        .getString("id");

                        Map<String, Object> attachBody = new HashMap<>();
                        attachBody.put("customer", customerId);
                        paymentMethods.attachPaymentMethod(paymentMethodId, attachBody)
                                        .then()
                                        .spec(ResponseSpec.OK());
                        TestContext.setPaymentMethodId(paymentMethodId);
                        logger.info("  Step 2: PaymentMethod created and attached → {}", paymentMethodId);
                } else {
                        logger.info("  Step 2: Reusing PaymentMethod from context → {}", paymentMethodId);
                }

                // 3️⃣ SetupIntent — always create fresh (E2E needs unconfirmed state)
                Map<String, Object> siBody = new HashMap<>();
                siBody.put("customer", customerId);
                siBody.put("payment_method", paymentMethodId);
                siBody.put("usage", "off_session");
                siBody.put("payment_method_types[0]", "card");

                String setupIntentId = SetupIntent.createSetupIntent(siBody)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("status", anyOf(equalTo("requires_payment_method"),
                                                equalTo("requires_confirmation")))
                                .extract()
                                .jsonPath()
                                .getString("id");

                setupIntentIdsToCleanup.add(setupIntentId);
                logger.info("  Step 3: SetupIntent created → {}", setupIntentId);

                // 4️⃣ Confirm the SetupIntent
                Map<String, Object> confirmBody = new HashMap<>();
                confirmBody.put("payment_method", paymentMethodId);

                SetupIntent.confirmSetupIntent(setupIntentId, confirmBody)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("status", equalTo("succeeded"));
                logger.info("  Step 4: SetupIntent confirmed ✅");

                // 5️⃣ Verify payment method is saved on the customer
                paymentMethods.retrievePaymentMethodByCustomer(customerId, paymentMethodId)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("id", equalTo(paymentMethodId))
                                .body("customer", equalTo(customerId));
                logger.info("  Step 5: PaymentMethod verified on customer ✅");

                // 6️⃣ Use the saved card for an off-session payment
                Map<String, Object> piBody = new HashMap<>();
                piBody.put("amount", amount);
                piBody.put("currency", "usd");
                piBody.put("customer", customerId);
                piBody.put("payment_method", paymentMethodId);
                piBody.put("off_session", true);
                piBody.put("confirm", true);
                piBody.put("automatic_payment_methods[enabled]", true);
                piBody.put("automatic_payment_methods[allow_redirects]", "never");

                PaymentIntent.createPaymentIntent(piBody)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("status", equalTo("succeeded"))
                                .body("customer", equalTo(customerId))
                                .body("payment_method", equalTo(paymentMethodId))
                                .body("amount", equalTo(amount));

                logger.info("  Step 6: Off-session payment succeeded ✅");
                logger.info("🎉 E2E Flow Complete: SetupIntent → Saved Card → Future Payment");
        }

        @Test(groups = { "setup_intent", "regression" })
        public void TC_16_positive_Idempotent_Confirm_Setup_Intent() {
                logger.info("Testing idempotent confirmation of SetupIntent");
                // Idempotency test needs a fresh unconfirmed SI each run — do not reuse
                // context.
                Map<String, Object> siBody = new HashMap<>();
                siBody.put("usage", "off_session");
                siBody.put("payment_method_types[0]", "card");

                String setupIntentId = SetupIntent.createSetupIntent(siBody)
                                .then()
                                .spec(ResponseSpec.OK())
                                .extract()
                                .jsonPath()
                                .getString("id");
                setupIntentIdsToCleanup.add(setupIntentId);
                logger.info("Created SetupIntent ID: {}", setupIntentId);

                // Reuse payment method from context if available
                String paymentMethodId = TestContext.getPaymentMethodId();
                if (paymentMethodId == null) {
                        paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(false);
                        TestContext.setPaymentMethodId(paymentMethodId);
                        logger.info("Created fallback payment method ID: {}", paymentMethodId);
                } else {
                        logger.info("Reusing payment method from context: {}", paymentMethodId);
                }

                Map<String, Object> confirmBody = new HashMap<>();
                confirmBody.put("payment_method", paymentMethodId);

                String idempotencyKey = "si_key_" + System.currentTimeMillis();
                Map<String, String> headers = new HashMap<>();
                headers.put("Idempotency-Key", idempotencyKey);
                logger.info("Using idempotency key: {}", idempotencyKey);

                // First confirm — Success
                logger.info("Sending first confirmation request");
                Response firstResp = SetupIntent.confirmSetupIntent(setupIntentId, confirmBody, headers)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("status", equalTo("succeeded"))
                                .extract()
                                .response();

                String firstId = firstResp.jsonPath().getString("id");
                logger.info("First confirmation succeeded. SetupIntent ID: {}", firstId);

                // Second confirm with same idempotency key — should return same result
                logger.info("Sending second confirmation request with same idempotency key");
                Response secondResp = SetupIntent.confirmSetupIntent(setupIntentId, confirmBody, headers)
                                .then()
                                .spec(ResponseSpec.OK())
                                .extract()
                                .response();

                String secondId = secondResp.jsonPath().getString("id");
                logger.info("Second confirmation succeeded. SetupIntent ID: {}", secondId);

                assertThat("Idempotent responses should return the same SetupIntent ID",
                                firstId, equalTo(secondId));

                logger.info("✅ Idempotent confirm verified for SetupIntent: {}", setupIntentId);
        }

        @Test(groups = { "setup_intent", "regression" })
        public void TC_17_positive_List_Setup_Intents() {
                logger.info("Testing list SetupIntents");
                // Reuse context SI if available; only create one if nothing exists yet
                String setupIntentId = TestContext.getSetupIntentId();
                if (setupIntentId == null) {
                        setupIntentId = SetupIntentHelper.createSetupIntent(false);
                        TestContext.setSetupIntentId(setupIntentId);
                        logger.info("Created fallback SetupIntent ID: {}", setupIntentId);
                } else {
                        logger.info("Reusing SetupIntent from context (ensures list is non-empty): {}", setupIntentId);
                }

                Map<String, Object> queryParams = new HashMap<>();
                queryParams.put("limit", 3);

                SetupIntent.listSetupIntents(queryParams)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("object", equalTo("list"))
                                .body("data.size()", greaterThanOrEqualTo(1))
                                .body("data[0].object", equalTo("setup_intent"));

                logger.info("✅ SetupIntents listed successfully");
        }

        // ═══════════════════════════════════════════════════════════════
        // ██ CLEANUP
        // ═══════════════════════════════════════════════════════════════

        @AfterClass(alwaysRun = true)
        public void cleanup() {
                logger.info("Running cleanup for SetupIntentTests");
                boolean isFlow = Arrays.asList(currentGroups).contains("flow");
                if (isFlow) {
                        logger.info("In flow mode, bypassing SetupIntentTests cleanup (delegated to suite cleanup)");
                        return; // Skip cleanup in flow runs to allow dependent classes/suite to clean up or
                                // proceed
                }

                logger.info("Canceling {} setup intents", setupIntentIdsToCleanup.size());
                for (String siId : setupIntentIdsToCleanup) {
                        try {
                                logger.info("Canceling SetupIntent ID: {}", siId);
                                SetupIntent.cancelSetupIntent(siId);
                        } catch (Exception e) {
                                // SetupIntent may already be canceled or succeeded — safe to ignore
                        }
                }
                setupIntentIdsToCleanup.clear();

                logger.info("Deleting {} customers", customerIdsToCleanup.size());
                for (String custId : customerIdsToCleanup) {
                        try {
                                logger.info("Deleting Customer ID: {}", custId);
                                Customer.deleteCustomer(custId);
                        } catch (Exception e) {
                                logger.error("⚠️ Cleanup failed for customer: {}", custId, e);
                        }
                }
                customerIdsToCleanup.clear();
        }
}

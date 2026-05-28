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

        // ═══════════════════════════════════════════════════════════════
        // ██ SETUP INTENT — CREATE
        // ═══════════════════════════════════════════════════════════════

        @Test(groups = { "flow", "unit" })
        public void TC_01_positive_Create_Setup_Intent() {
                logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

                boolean doesCustomerExist = false;
                // Ensure customer exists
                String customerId = TestContext.getCustomerId();
                if (customerId == null) {
                        customerId = SetupIntentHelper.createFallbackCustomer();
                        customerIdsToCleanup.add(customerId);
                        doesCustomerExist = true;
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

                if (!doesCustomerExist) {
                        TestContext.setSetupIntentId(setupIntentId);
                }
                setupIntentIdsToCleanup.add(setupIntentId);
                logger.info("✅ SetupIntent created: {}", setupIntentId);
        }

        @Test(groups = { "unit" })
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

        @Test(groups = { "unit" })
        public void TC_03_positive_Create_Setup_Intent_OnSession() {
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

        @Test(groups = { "flow", "unit" })
        public void TC_04_positive_Confirm_Setup_Intent() {
                logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

                String setupIntentId = TestContext.getSetupIntentId();
                if (setupIntentId == null) {
                        setupIntentId = SetupIntentHelper.createSetupIntent(true);
                        setupIntentIdsToCleanup.add(setupIntentId);
                }

                // Create and use a valid payment method for confirmation
                String paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(false);

                Map<String, Object> confirmBody = new HashMap<>();
                confirmBody.put("payment_method", paymentMethodId);

                SetupIntent.confirmSetupIntent(setupIntentId, confirmBody)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("status", equalTo("succeeded"))
                                .body("payment_method", equalTo(paymentMethodId))
                                .body("object", equalTo("setup_intent"));

                logger.info("✅ SetupIntent confirmed: {}", setupIntentId);
        }

        @Test(groups = { "unit" }, dataProvider = "declinedCardTokensForSetupIntent", dataProviderClass = SetupIntentDataProvider.class)
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

                // Attempt to confirm — should fail
                Map<String, Object> confirmBody = new HashMap<>();
                confirmBody.put("payment_method", declinedPmId);

                SetupIntent.confirmSetupIntent(setupIntentId, confirmBody)
                                .then()
                                .spec(ResponseSpec.request_failed())
                                .body("error.code", containsString(expectedErrorCode));

                logger.info("✅ Correctly declined: {} → {}", testCaseName, expectedErrorCode);
        }

        @Test(groups = { "unit" })
        public void TC_06_negative_Confirm_Already_Succeeded_Setup_Intent() {
                // Create and confirm a SetupIntent first
                String setupIntentId = SetupIntentHelper.createAndConfirmSetupIntent();
                setupIntentIdsToCleanup.add(setupIntentId);

                String paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(false);

                Map<String, Object> confirmBody = new HashMap<>();
                confirmBody.put("payment_method", paymentMethodId);

                // Attempting to confirm an already succeeded SetupIntent
                SetupIntent.confirmSetupIntent(setupIntentId, confirmBody)
                                .then()
                                .spec(ResponseSpec.request_failed())
                                .body("error.code", equalTo("setup_intent_unexpected_state"));

                logger.info("✅ Correctly rejected re-confirmation of succeeded SetupIntent");
        }

        @Test(groups = { "unit" })
        public void TC_07_negative_Confirm_Canceled_Setup_Intent() {
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

                setupIntentIdsToCleanup.add(setupIntentId);

                // Cancel it
                SetupIntent.cancelSetupIntent(setupIntentId)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("status", equalTo("canceled"));

                // Attempt to confirm a canceled SetupIntent
                String paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(false);
                Map<String, Object> confirmBody = new HashMap<>();
                confirmBody.put("payment_method", paymentMethodId);

                SetupIntent.confirmSetupIntent(setupIntentId, confirmBody)
                                .then()
                                .spec(ResponseSpec.request_failed())
                                .body("error.code", equalTo("setup_intent_unexpected_state"));

                logger.info("✅ Correctly rejected confirmation of canceled SetupIntent");
        }

        // ═══════════════════════════════════════════════════════════════
        // ██ SETUP INTENT — RETRIEVE
        // ═══════════════════════════════════════════════════════════════

        @Test(groups = { "unit" })
        public void TC_08_positive_Retrieve_Setup_Intent() {
                String setupIntentId = SetupIntentHelper.createSetupIntent(false);
                setupIntentIdsToCleanup.add(setupIntentId);

                SetupIntent.retrieveSetupIntent(setupIntentId)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("id", equalTo(setupIntentId))
                                .body("object", equalTo("setup_intent"));

                logger.info("✅ SetupIntent retrieved: {}", setupIntentId);
        }

        @Test(groups = { "unit" }, dataProvider = "invalidSetupIntentIds", dataProviderClass = SetupIntentDataProvider.class)
        public void TC_09_negative_Retrieve_Invalid_Setup_Intent(String invalidId,
                        io.restassured.specification.ResponseSpecification expectedSpec) {

                SetupIntent.retrieveSetupIntent(invalidId)
                                .then()
                                .spec(expectedSpec);

                logger.info("✅ Correctly failed retrieval for invalid ID: {}", invalidId);
        }

        // ═══════════════════════════════════════════════════════════════
        // ██ SETUP INTENT — UPDATE
        // ═══════════════════════════════════════════════════════════════

        @Test(groups = { "unit" }, dataProvider = "setupIntentMetadataUpdates", dataProviderClass = SetupIntentDataProvider.class)
        public void TC_10_positive_Update_Setup_Intent_Metadata(String key, String value) {

                String setupIntentId = SetupIntentHelper.createSetupIntent(false);
                setupIntentIdsToCleanup.add(setupIntentId);

                Map<String, Object> updateBody = new HashMap<>();
                updateBody.put("metadata[" + key + "]", value);

                SetupIntent.updateSetupIntent(setupIntentId, updateBody)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("metadata." + key, equalTo(value));

                logger.info("✅ SetupIntent metadata updated: {}={}", key, value);
        }

        @Test(groups = { "unit" })
        public void TC_11_negative_Update_Nonexistent_Setup_Intent() {
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

        @Test(groups = { "unit" }, dataProvider = "cancellationReasons", dataProviderClass = SetupIntentDataProvider.class)
        public void TC_12_positive_Cancel_Setup_Intent(String reason) {
                String setupIntentId = SetupIntentHelper.createSetupIntent(false);
                setupIntentIdsToCleanup.add(setupIntentId);

                Map<String, Object> cancelBody = new HashMap<>();
                cancelBody.put("cancellation_reason", reason);

                SetupIntent.cancelSetupIntent(setupIntentId, cancelBody)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("status", equalTo("canceled"))
                                .body("cancellation_reason", equalTo(reason));

                logger.info("✅ SetupIntent canceled with reason: {}", reason);
        }

        @Test(groups = { "unit" })
        public void TC_13_negative_Cancel_Already_Canceled_Setup_Intent() {
                String setupIntentId = SetupIntentHelper.createSetupIntent(false);
                setupIntentIdsToCleanup.add(setupIntentId);

                // First cancel succeeds
                SetupIntent.cancelSetupIntent(setupIntentId)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("status", equalTo("canceled"));

                // Second cancel fails
                SetupIntent.cancelSetupIntent(setupIntentId)
                                .then()
                                .spec(ResponseSpec.request_failed())
                                .body("error.code", equalTo("setup_intent_unexpected_state"));

                logger.info("✅ Correctly rejected double-cancellation of SetupIntent");
        }

        @Test(groups = { "unit" })
        public void TC_14_negative_Cancel_Succeeded_Setup_Intent() {
                String setupIntentId = SetupIntentHelper.createAndConfirmSetupIntent();
                setupIntentIdsToCleanup.add(setupIntentId);

                SetupIntent.cancelSetupIntent(setupIntentId)
                                .then()
                                .spec(ResponseSpec.request_failed())
                                .body("error.code", equalTo("setup_intent_unexpected_state"));

                logger.info("✅ Correctly rejected cancellation of succeeded SetupIntent");
        }

        // ═══════════════════════════════════════════════════════════════
        // ██ SAVED CARD + FUTURE PAYMENT — E2E FLOW
        // ═══════════════════════════════════════════════════════════════

        @Test(groups = { "flow", "unit" })
        public void TC_15_flow_Saved_Card_Future_OffSession_Payment() {
                logger.info("Test running under groups: {}", Arrays.toString(currentGroups));
                logger.info("🔄 Starting E2E: SetupIntent → Saved Card → Off-Session Payment");

                // 1️⃣ Create a customer
                String name = CustomersHelper.getName();
                String email = faker.internet().safeEmailAddress();
                Response custResp = Customer.createCustomer(name, email, null);
                String customerId = custResp.jsonPath().getString("id");
                customerIdsToCleanup.add(customerId);
                logger.info("  Step 1: Customer created → {}", customerId);

                // 2️⃣ Create a payment method and attach to customer
                Map<String, Object> pmBody = new HashMap<>();
                pmBody.put("type", "card");
                pmBody.put("card[token]", "tok_visa");
                pmBody.put("billing_details[email]", email);
                pmBody.put("billing_details[name]", name);
                String paymentMethodId = paymentMethods.createPaymentMethod(pmBody)
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
                logger.info("  Step 2: PaymentMethod attached → {}", paymentMethodId);

                // 3️⃣ Create a SetupIntent for off_session usage
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
                piBody.put("amount", 5000);
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
                                .body("amount", equalTo(5000));

                logger.info("  Step 6: Off-session payment succeeded ✅");
                logger.info("🎉 E2E Flow Complete: SetupIntent → Saved Card → Future Payment");
        }

        @Test(groups = { "unit" })
        public void TC_16_positive_Idempotent_Confirm_Setup_Intent() {
                // Create a SetupIntent
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

                String paymentMethodId = PaymentMethodsHelper.createValidPaymentMethod(false);

                Map<String, Object> confirmBody = new HashMap<>();
                confirmBody.put("payment_method", paymentMethodId);

                String idempotencyKey = "si_key_" + System.currentTimeMillis();
                Map<String, String> headers = new HashMap<>();
                headers.put("Idempotency-Key", idempotencyKey);

                // First confirm — Success
                Response firstResp = SetupIntent.confirmSetupIntent(setupIntentId, confirmBody, headers)
                                .then()
                                .spec(ResponseSpec.OK())
                                .body("status", equalTo("succeeded"))
                                .extract()
                                .response();

                String firstId = firstResp.jsonPath().getString("id");

                // Second confirm with same idempotency key — should return same result
                Response secondResp = SetupIntent.confirmSetupIntent(setupIntentId, confirmBody, headers)
                                .then()
                                .spec(ResponseSpec.OK())
                                .extract()
                                .response();

                String secondId = secondResp.jsonPath().getString("id");

                assertThat("Idempotent responses should return the same SetupIntent ID",
                                firstId, equalTo(secondId));

                logger.info("✅ Idempotent confirm verified for SetupIntent: {}", setupIntentId);
        }

        @Test(groups = { "unit" })
        public void TC_17_positive_List_Setup_Intents() {
                // Ensure at least one SetupIntent exists
                SetupIntentHelper.createSetupIntent(false);

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
                boolean isFlow = Arrays.asList(currentGroups).contains("flow");
                if (isFlow) {
                        return; // Skip cleanup in flow runs to allow dependent classes/suite to clean up or
                                // proceed
                }

                for (String siId : setupIntentIdsToCleanup) {
                        try {
                                SetupIntent.cancelSetupIntent(siId);
                        } catch (Exception e) {
                                // SetupIntent may already be canceled or succeeded — safe to ignore
                        }
                }
                setupIntentIdsToCleanup.clear();

                for (String custId : customerIdsToCleanup) {
                        try {
                                Customer.deleteCustomer(custId);
                        } catch (Exception e) {
                                System.out.println("⚠️ Cleanup failed for customer: " + custId);
                        }
                }
                customerIdsToCleanup.clear();
        }
}

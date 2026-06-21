package tests;

import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataprovider.TransfersDataProvider;
import endpoints.ConnectAccounts;
import endpoints.Transfers;
import helpers.ConnectedAccountHelper;
import helpers.TransfersHelper;
import io.restassured.response.Response;
import helpers.TestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import specification.ResponseSpec;
import testbase.BaseClass;

public class TransferTests extends BaseClass {

    List<String> fallbackConnectAccountIds = new ArrayList<>();
    List<String> fallbackTransferIds = new ArrayList<>();

    // ***************CREATE TRANSFER – POSITIVE*******************\\

    @Test(groups = { "transfer", "regression" })
    public void TC_01_Create_Valid_Transfer() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

        String connectAccountId = TestContext.getConnectAccountId();
        if (connectAccountId == null) {
            connectAccountId = ConnectedAccountHelper.createConnectAccount(false);
            fallbackConnectAccountIds.add(connectAccountId);
            logger.info("Created new connect account ID --> {}", connectAccountId);
        } else {
            logger.info("Fetched connect account ID from context --> {}", connectAccountId);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("amount", amount / 2);
        body.put("currency", "usd");
        body.put("destination", connectAccountId);

        String transferId = Transfers.createTransfer(body)
                .then()
                .spec(ResponseSpec.OK())
                .body("amount", equalTo(amount / 2))
                .body("currency", equalTo("usd"))
                .body("destination", equalTo(connectAccountId))
                .extract()
                .jsonPath()
                .getString("id");

        logger.info("Created Transfer ID: {}", transferId);
        TestContext.setTransferId(transferId);
    }

    // ***************RETRIEVE TRANSFER – POSITIVE*******************\\

    @Test(groups = { "transfer", "regression" })
    public void TC_02_Retrieve_Transfer() {
        String transferId = TestContext.getTransferId();
        logger.info("Fetched transfer ID from context --> {}", transferId);
        if (transferId == null) {
            transferId = TransfersHelper.createFallbackTransfer();
            fallbackTransferIds.add(transferId);
            logger.info("Created fallback transfer ID --> {}", transferId);
        } else {
            logger.info("Using active transfer ID --> {}", transferId);
        }

        logger.info("Retrieving transfer with ID: {}", transferId);
        Transfers.retrieveTransfer(transferId)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(transferId));
        logger.info("Successfully retrieved transfer with ID: {}", transferId);
    }

    // ***************REVERSE TRANSFER – POSITIVE*******************\\

    @Test(groups = { "transfer", "regression" })
    public void TC_03_Reverse_Transfer() {
        String transferId = TestContext.getTransferId();
        logger.info("Fetched transfer ID from context --> {}", transferId);
        if (transferId == null) {
            transferId = TransfersHelper.createFallbackTransfer();
            fallbackTransferIds.add(transferId);
            logger.info("Created fallback transfer ID --> {}", transferId);
        } else {
            logger.info("Using active transfer ID --> {}", transferId);
        }

        logger.info("Reversing transfer with ID: {}", transferId);
        Map<String, Object> body = new HashMap<>(); // Empty body reverses full amount

        Transfers.reverseTransfer(transferId, body)
                .then()
                .spec(ResponseSpec.OK())
                .body("transfer", equalTo(transferId));
        logger.info("Successfully reversed transfer with ID: {}", transferId);
    }

    // ***************CREATE TRANSFER – NEGATIVE*******************\\

    @Test(groups = { "transfer", "negative", "regression" })
    public void TC_04_CreateTransfer_InvalidDestination() {
        logger.info("Testing create transfer with invalid destination");
        Map<String, Object> body = new HashMap<>();
        body.put("amount", amount / 2);
        body.put("currency", "usd");
        body.put("destination", "acct_invalid_12345");

        Transfers.createTransfer(body)
                .then()
                .spec(ResponseSpec.bad_request())
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", containsString("No such account"));
        logger.info("Successfully verified invalid destination error");
    }

    @Test(groups = { "transfer", "negative", "regression" })
    public void TC_05_CreateTransfer_NegativeAmount() {
        logger.info("Testing create transfer with negative amount");
        String connectAccountId = ConnectedAccountHelper.createConnectAccount(false);
        logger.info("Created connect account ID for TC_05: {}", connectAccountId);

        Map<String, Object> body = new HashMap<>();
        body.put("amount", -100);
        body.put("currency", "usd");
        body.put("destination", connectAccountId);

        Transfers.createTransfer(body)
                .then()
                .spec(ResponseSpec.bad_request());
        logger.info("Successfully verified negative amount rejection");
    }

    @Test(groups = { "transfer", "negative", "regression" })
    public void TC_06_CreateTransfer_ZeroAmount() {
        logger.info("Testing create transfer with zero amount");
        String connectAccountId = ConnectedAccountHelper.createConnectAccount(false);
        logger.info("Created connect account ID for TC_06: {}", connectAccountId);

        Map<String, Object> body = new HashMap<>();
        body.put("amount", 0);
        body.put("currency", "usd");
        body.put("destination", connectAccountId);

        Transfers.createTransfer(body)
                .then()
                .spec(ResponseSpec.bad_request());
        logger.info("Successfully verified zero amount rejection");
    }

    @Test(groups = { "transfer", "negative", "regression" })
    public void TC_07_CreateTransfer_InvalidCurrency() {
        logger.info("Testing create transfer with invalid currency");
        String connectAccountId = ConnectedAccountHelper.createConnectAccount(false);
        logger.info("Created connect account ID for TC_07: {}", connectAccountId);

        Map<String, Object> body = new HashMap<>();
        body.put("amount", 1000);
        body.put("currency", "invalid_curr");
        body.put("destination", connectAccountId);

        Transfers.createTransfer(body)
                .then()
                .spec(ResponseSpec.bad_request());
        logger.info("Successfully verified invalid currency rejection");
    }

    @Test(groups = { "transfer", "negative",
            "regression" }, dataProvider = "invalidTransferPayloads", dataProviderClass = TransfersDataProvider.class)
    public void TC_08_CreateTransfer_MissingRequiredFields(String testCaseName, Map<String, Object> body) {
        logger.info("Running invalid transfer payload case: {}", testCaseName);

        Transfers.createTransfer(body)
                .then()
                .spec(ResponseSpec.bad_request());
        logger.info("Successfully verified missing required fields rejection");
    }

    @Test(groups = { "transfer", "negative", "auth", "regression" })
    public void TC_09_CreateTransfer_InvalidAuth() {
        logger.info("Testing create transfer with invalid auth");
        Map<String, Object> body = new HashMap<>();
        body.put("amount", amount / 2);
        body.put("currency", "usd");
        body.put("destination", "acct_any_id");

        Transfers.createTransferWithCustomAuth("sk_test_invalid_key_12345", body)
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));
        logger.info("Successfully verified invalid auth error");
    }

    @Test(groups = { "transfer", "negative", "auth", "regression" })
    public void TC_10_CreateTransfer_MissingAuth() {
        logger.info("Testing create transfer with missing auth");
        Map<String, Object> body = new HashMap<>();
        body.put("amount", amount / 2);
        body.put("currency", "usd");
        body.put("destination", "acct_any_id");

        Transfers.createTransferWithCustomAuth(null, body)
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("You did not provide an API key"));
        logger.info("Successfully verified missing auth error");
    }

    // ***************RETRIEVE TRANSFER – NEGATIVE*******************\\

    @Test(groups = { "transfer", "negative",
            "regression" }, dataProvider = "invalidTransferIds", dataProviderClass = TransfersDataProvider.class)
    public void TC_11_RetrieveTransfer_InvalidId(String testCaseName, String transferId, String expectedErrorFragment) {
        logger.info("Testing retrieve transfer with invalid ID: {} -> {}", testCaseName, transferId);
        Transfers.retrieveTransfer(transferId)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.message", containsString(expectedErrorFragment));
        logger.info("Successfully verified invalid ID error fragment: {}", expectedErrorFragment);
    }

    @Test(groups = { "transfer", "negative", "auth", "regression" })
    public void TC_12_RetrieveTransfer_InvalidAuth() {
        logger.info("Testing retrieve transfer with invalid auth");
        Transfers.retrieveTransferWithCustomAuth("sk_test_invalid_key_12345", "tr_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));
        logger.info("Successfully verified invalid auth error");
    }

    @Test(groups = { "transfer", "negative", "auth", "regression" })
    public void TC_13_RetrieveTransfer_MissingAuth() {
        logger.info("Testing retrieve transfer with missing auth");
        Transfers.retrieveTransferWithCustomAuth(null, "tr_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("You did not provide an API key"));
        logger.info("Successfully verified missing auth error");
    }

    // ***************REVERSE TRANSFER – NEGATIVE*******************\\

    @Test(groups = { "transfer", "negative", "regression" })
    public void TC_14_ReverseTransfer_InvalidId() {
        logger.info("Testing reverse transfer with invalid ID");
        Map<String, Object> body = new HashMap<>();
        Transfers.reverseTransfer("tr_invalid_id_12345", body)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.message", containsString("No such transfer"));
        logger.info("Successfully verified invalid ID error for reverse transfer");
    }

    @Test(groups = { "transfer", "negative", "regression" })
    public void TC_15_ReverseTransfer_AmountExceedsOriginal() {
        logger.info("Testing reverse transfer with amount exceeding original");
        String transferId = TransfersHelper.createFallbackTransfer();
        fallbackTransferIds.add(transferId);
        logger.info("Created fallback transfer ID for TC_15: {}", transferId);

        Map<String, Object> body = new HashMap<>();
        body.put("amount", 33000); // Original is 1000

        Transfers.reverseTransfer(transferId, body)
                .then()
                .spec(ResponseSpec.bad_request())
                .body("error.message", containsString("cannot be greater than"));
        logger.info("Successfully verified amount exceeding original error");
    }

    @Test(groups = { "transfer", "negative", "regression" })
    public void TC_16_ReverseTransfer_ZeroAmount() {
        logger.info("Testing reverse transfer with zero amount");
        String transferId = TransfersHelper.createFallbackTransfer();
        logger.info("Created fallback transfer ID for TC_16: {}", transferId);

        Map<String, Object> body = new HashMap<>();
        body.put("amount", 0);

        Transfers.reverseTransfer(transferId, body)
                .then()
                .spec(ResponseSpec.bad_request());
        logger.info("Successfully verified zero amount error for reverse transfer");
    }

    @Test(groups = { "transfer", "negative", "auth", "regression" })
    public void TC_17_ReverseTransfer_InvalidAuth() {
        logger.info("Testing reverse transfer with invalid auth");
        Map<String, Object> body = new HashMap<>();
        Transfers.reverseTransferWithCustomAuth("sk_test_invalid_key_12345", "tr_any_id", body)
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));
        logger.info("Successfully verified invalid auth error");
    }

    @Test(groups = { "transfer", "negative", "auth", "regression" })
    public void TC_18_ReverseTransfer_MissingAuth() {
        logger.info("Testing reverse transfer with missing auth");
        Map<String, Object> body = new HashMap<>();
        Transfers.reverseTransferWithCustomAuth(null, "tr_any_id", body)
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("You did not provide an API key"));
        logger.info("Successfully verified missing auth error");
    }

    @Test(groups = { "transfer", "regression" })
    public void TC_19_positive_Idempotent_CreateTransfer() {
        logger.info("Testing idempotent create transfer");
        String connectAccountId = ConnectedAccountHelper.createConnectAccount(false);
        logger.info("Created connect account ID for TC_19: {}", connectAccountId);

        Map<String, Object> body = new HashMap<>();
        body.put("amount", amount / 2);
        body.put("currency", "usd");
        body.put("destination", connectAccountId);

        Map<String, String> headers = new HashMap<>();
        String idempotencyKey = "trans_key_" + System.currentTimeMillis();
        headers.put("Idempotency-Key", idempotencyKey);
        logger.info("Using idempotency key: {}", idempotencyKey);

        Response firstResponse = Transfers.createTransfer(body, headers)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .response();

        String firstTransferId = firstResponse.jsonPath().getString("id");
        logger.info("First response Transfer ID: {}", firstTransferId);

        Response secondResponse = Transfers.createTransfer(body, headers)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .response();

        String secondTransferId = secondResponse.jsonPath().getString("id");
        logger.info("Second response Transfer ID: {}", secondTransferId);

        org.testng.Assert.assertEquals(firstTransferId, secondTransferId);
        logger.info("Verified transfer IDs are equal (Idempotency success)");
    }

    // ***************CLEANUP*******************\\

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        logger.info("🧹 Starting cleanup for TransferTests...");

        // Stripe transfers cannot be deleted via the API (only reversed),
        // so log any fallback transfer IDs created during the test run for
        // manual audit or reference in the Stripe dashboard.
        if (!fallbackTransferIds.isEmpty()) {
            logger.info("ℹ️ {} fallback transfer(s) were created during the test run (cannot be deleted via API):",
                    fallbackTransferIds.size());
            for (String transferId : fallbackTransferIds) {
                logger.info("   - Transfer ID: {}", transferId);
            }
        }

        // Delete all connected accounts that were created as fallbacks.
        for (String accountId : fallbackConnectAccountIds) {
            try {
                ConnectAccounts.deleteConnectAccount(accountId);
                logger.info("🧹 Deleted fallback connect account: {}", accountId);
            } catch (Exception e) {
                logger.warn("⚠️ Failed to delete connect account {}: {}", accountId, e.getMessage());
            }
        }

        // Clear the shared transfer ID and connect account ID from TestContext
        // to avoid state leakage into test classes that run after TransferTests.
        TestContext.setTransferId(null);
        TestContext.setConnectAccountId(null);
        logger.info("🧹 Cleared transfer and connect account IDs from TestContext.");

        logger.info("✅ Cleanup complete for TransferTests.");
    }
}

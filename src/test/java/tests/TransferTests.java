package tests;

import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import dataprovider.TransfersDataProvider;
import endpoints.Transfers;
import helpers.ConnectedAccountHelper;
import helpers.TransfersHelper;
import helpers.TestContext;
import org.testng.annotations.Test;
import specification.ResponseSpec;
import testbase.BaseClass;

public class TransferTests extends BaseClass {

    // ***************CREATE TRANSFER – POSITIVE*******************\\

    @Test(groups = { "unit" })
    public void TC_01_Create_Valid_Transfer() {
        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

        String connectAccountId = ConnectedAccountHelper.createConnectAccount(false);
        logger.info("Created connect account ID for transfer: {}", connectAccountId);

        Map<String, Object> body = new HashMap<>();
        body.put("amount", 1000);
        body.put("currency", "usd");
        body.put("destination", connectAccountId);

        String transferId = Transfers.createTransfer(body)
                .then()
                .spec(ResponseSpec.OK())
                .body("amount", equalTo(1000))
                .body("currency", equalTo("usd"))
                .body("destination", equalTo(connectAccountId))
                .extract()
                .jsonPath()
                .getString("id");

        logger.info("Created Transfer ID: {}", transferId);
        TestContext.setTransferId(transferId);
    }

    // ***************RETRIEVE TRANSFER – POSITIVE*******************\\

    @Test(groups = { "unit" })
    public void TC_02_Retrieve_Transfer() {
        String transferId = TestContext.getTransferId();

        if (transferId == null) {
            transferId = TransfersHelper.createFallbackTransfer();
            logger.info("Created fallback transfer ID: {}", transferId);
        }

        Transfers.retrieveTransfer(transferId)
                .then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(transferId));
    }

    // ***************REVERSE TRANSFER – POSITIVE*******************\\

    @Test(groups = { "unit" })
    public void TC_03_Reverse_Transfer() {
        String transferId = TestContext.getTransferId();

        if (transferId == null) {
            transferId = TransfersHelper.createFallbackTransfer();
            logger.info("Created fallback transfer ID: {}", transferId);
        }

        Map<String, Object> body = new HashMap<>(); // Empty body reverses full amount

        Transfers.reverseTransfer(transferId, body)
                .then()
                .spec(ResponseSpec.OK())
                .body("transfer", equalTo(transferId));
    }

    // ***************CREATE TRANSFER – NEGATIVE*******************\\

    @Test(groups = { "unit" })
    public void TC_04_CreateTransfer_InvalidDestination() {
        Map<String, Object> body = new HashMap<>();
        body.put("amount", 1000);
        body.put("currency", "usd");
        body.put("destination", "acct_invalid_12345");

        Transfers.createTransfer(body)
                .then()
                .spec(ResponseSpec.bad_request())
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", containsString("No such account"));
    }

    @Test(groups = { "unit" })
    public void TC_05_CreateTransfer_NegativeAmount() {
        String connectAccountId = ConnectedAccountHelper.createConnectAccount(false);

        Map<String, Object> body = new HashMap<>();
        body.put("amount", -100);
        body.put("currency", "usd");
        body.put("destination", connectAccountId);

        Transfers.createTransfer(body)
                .then()
                .spec(ResponseSpec.bad_request());
    }

    @Test(groups = { "unit" })
    public void TC_06_CreateTransfer_ZeroAmount() {
        String connectAccountId = ConnectedAccountHelper.createConnectAccount(false);

        Map<String, Object> body = new HashMap<>();
        body.put("amount", 0);
        body.put("currency", "usd");
        body.put("destination", connectAccountId);

        Transfers.createTransfer(body)
                .then()
                .spec(ResponseSpec.bad_request());
    }

    @Test(groups = { "unit" })
    public void TC_07_CreateTransfer_InvalidCurrency() {
        String connectAccountId = ConnectedAccountHelper.createConnectAccount(false);

        Map<String, Object> body = new HashMap<>();
        body.put("amount", 1000);
        body.put("currency", "invalid_curr");
        body.put("destination", connectAccountId);

        Transfers.createTransfer(body)
                .then()
                .spec(ResponseSpec.bad_request());
    }

    @Test(groups = { "unit" }, dataProvider = "invalidTransferPayloads", dataProviderClass = TransfersDataProvider.class)
    public void TC_08_CreateTransfer_MissingRequiredFields(String testCaseName, Map<String, Object> body) {
        logger.info("Running invalid transfer payload case: {}", testCaseName);

        Transfers.createTransfer(body)
                .then()
                .spec(ResponseSpec.bad_request());
    }

    @Test(groups = { "unit" })
    public void TC_09_CreateTransfer_InvalidAuth() {
        Map<String, Object> body = new HashMap<>();
        body.put("amount", 1000);
        body.put("currency", "usd");
        body.put("destination", "acct_any_id");

        Transfers.createTransferWithCustomAuth("sk_test_invalid_key_12345", body)
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));
    }

    @Test(groups = { "unit" })
    public void TC_10_CreateTransfer_MissingAuth() {
        Map<String, Object> body = new HashMap<>();
        body.put("amount", 1000);
        body.put("currency", "usd");
        body.put("destination", "acct_any_id");

        Transfers.createTransferWithCustomAuth(null, body)
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("You did not provide an API key"));
    }

    // ***************RETRIEVE TRANSFER – NEGATIVE*******************\\

    @Test(groups = { "unit" }, dataProvider = "invalidTransferIds", dataProviderClass = TransfersDataProvider.class)
    public void TC_11_RetrieveTransfer_InvalidId(String testCaseName, String transferId, String expectedErrorFragment) {
        Transfers.retrieveTransfer(transferId)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.message", containsString(expectedErrorFragment));
    }

    @Test(groups = { "unit" })
    public void TC_12_RetrieveTransfer_InvalidAuth() {
        Transfers.retrieveTransferWithCustomAuth("sk_test_invalid_key_12345", "tr_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));
    }

    @Test(groups = { "unit" })
    public void TC_13_RetrieveTransfer_MissingAuth() {
        Transfers.retrieveTransferWithCustomAuth(null, "tr_any_id")
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("You did not provide an API key"));
    }

    // ***************REVERSE TRANSFER – NEGATIVE*******************\\

    @Test(groups = { "unit" })
    public void TC_14_ReverseTransfer_InvalidId() {
        Map<String, Object> body = new HashMap<>();
        Transfers.reverseTransfer("tr_invalid_id_12345", body)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.message", containsString("No such transfer"));
    }

    @Test(groups = { "unit" })
    public void TC_15_ReverseTransfer_AmountExceedsOriginal() {
        String transferId = TransfersHelper.createFallbackTransfer();

        Map<String, Object> body = new HashMap<>();
        body.put("amount", 2000); // Original is 1000

        Transfers.reverseTransfer(transferId, body)
                .then()
                .spec(ResponseSpec.bad_request())
                .body("error.message", containsString("cannot be greater than"));
    }

    @Test(groups = { "unit" })
    public void TC_16_ReverseTransfer_ZeroAmount() {
        String transferId = TransfersHelper.createFallbackTransfer();

        Map<String, Object> body = new HashMap<>();
        body.put("amount", 0);

        Transfers.reverseTransfer(transferId, body)
                .then()
                .spec(ResponseSpec.bad_request());
    }

    @Test(groups = { "unit" })
    public void TC_17_ReverseTransfer_InvalidAuth() {
        Map<String, Object> body = new HashMap<>();
        Transfers.reverseTransferWithCustomAuth("sk_test_invalid_key_12345", "tr_any_id", body)
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));
    }

    @Test(groups = { "unit" })
    public void TC_18_ReverseTransfer_MissingAuth() {
        Map<String, Object> body = new HashMap<>();
        Transfers.reverseTransferWithCustomAuth(null, "tr_any_id", body)
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("You did not provide an API key"));
    }
}

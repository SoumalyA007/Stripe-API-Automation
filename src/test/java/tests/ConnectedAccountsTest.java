package tests;

import dataprovider.ConnectedAccountsDataProvider;
import endpoints.ConnectAccounts;
import helpers.ConnectedAccountHelper;
import helpers.TestContext;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import specification.ResponseSpec;
import testbase.BaseClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;

public class ConnectedAccountsTest extends BaseClass {

    List<String> connectAccountIdToCleanup = new ArrayList<>();

    @Test(groups = { "unit" })
    public void TC_01_positive_Create_ConnectAccount() {
        boolean isFlow = false;
        Map<String, Object> body = new HashMap<>();
        body.put("type", "express");
        if (TestContext.getBillingEmail() != null) {
            body.put("email", TestContext.getBillingEmail());
            isFlow = true;
        }

        String connectAccountId = ConnectAccounts.createConnectAccount(body)
                .then()
                .body("object", equalTo("account"))
                .body("id", notNullValue())
                .extract()
                .jsonPath()
                .get("id");
        if (isFlow == true) {
            TestContext.setConnectAccountId(connectAccountId);
        } else {
            connectAccountIdToCleanup.add(connectAccountId);
        }
    }

    @Test(groups = { "unit" })
    public void TC_02_positive_Update_ConnectAccount() {
        String connectAccountId = TestContext.getConnectAccountId();
        boolean isFlow = true;
        if (connectAccountId == null) {
            connectAccountId = ConnectedAccountHelper.createConnectAccount(false);
            isFlow = false;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("default_currency", "usd");
        ConnectAccounts.updateConnectAccount(connectAccountId, body)
                .then()
                .body("default_currency", equalTo("usd"))
                .body("id", equalTo(connectAccountId));

        if (isFlow == false) {
            connectAccountIdToCleanup.add(connectAccountId);
        }
    }

    @Test(groups = { "unit" })
    public void TC_03_positive_Delete_ConnectAccount() {
        String connectAccountId = TestContext.getConnectAccountId();
        boolean isFlow = true;
        if (connectAccountId == null) {
            connectAccountId = ConnectedAccountHelper.createConnectAccount(false);
            isFlow = false;
        }
        ConnectAccounts.deleteConnectAccount(connectAccountId)
                .then()
                .body("id", equalTo(connectAccountId));
        if (isFlow == false) {
            connectAccountIdToCleanup.add(connectAccountId);
        }
    }

    @Test(groups = { "unit" })
    public void TC_04_positive_Retrieve_ConnectAccount() {
        String connectAccountId = TestContext.getConnectAccountId();
        boolean isFlow = true;
        if (connectAccountId == null) {
            connectAccountId = ConnectedAccountHelper.createConnectAccount(false);
            isFlow = false;
        }
        ConnectAccounts.retrieveConnectAccount(connectAccountId)
                .then()
                .body("id", equalTo(connectAccountId));
        if (isFlow == false) {
            connectAccountIdToCleanup.add(connectAccountId);
        }
    }

    // ***************CREATE CONNECT ACCOUNT – NEGATIVE*******************\\

    @Test(groups = {
            "unit" }, dataProvider = "invalidConnectAccountPayloads", dataProviderClass = ConnectedAccountsDataProvider.class)
    public void TC_05_negative_Create_ConnectAccount_Invalid(String testCaseName, Map<String, Object> body) {
        logger.info("Running invalid connect account payload case: {}", testCaseName);

        Response response = ConnectAccounts.createConnectAccount(body);

        if (response.getStatusCode() == 200) {
            String connectAccountId = response.jsonPath().getString("id");
            connectAccountIdToCleanup.add(connectAccountId);
        }

        response.then()
                .spec(ResponseSpec.bad_request())
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", notNullValue());

        logger.info("✅ Correctly rejected invalid connect account payload: {}", testCaseName);
    }

    // ***************RETRIEVE CONNECT ACCOUNT – NEGATIVE*******************\\

    @Test(groups = {
            "unit" }, dataProvider = "invalidAccountIds", dataProviderClass = ConnectedAccountsDataProvider.class)
    public void TC_06_negative_Retrieve_ConnectAccount_InvalidId(String invalidAccountId) {
        logger.info("Retrieving invalid connect account ID: {}", invalidAccountId);

        ConnectAccounts.retrieveConnectAccount(invalidAccountId)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", containsString("No such account"));

        logger.info("✅ Correctly rejected retrieval of invalid connect account ID: {}", invalidAccountId);
    }

    // ***************UPDATE CONNECT ACCOUNT – NEGATIVE*******************\\

    @Test(groups = { "unit" })
    public void TC_07_negative_Update_ConnectAccount_NonexistentId() {
        logger.info("Updating nonexistent connect account");

        Map<String, Object> body = new HashMap<>();
        body.put("default_currency", "usd");

        ConnectAccounts.updateConnectAccount("acct_invalid_12345", body)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", containsString("No such account"));

        logger.info("✅ Correctly rejected updating nonexistent connect account");
    }

    @Test(groups = {
            "unit" }, dataProvider = "invalidUpdatePayloads", dataProviderClass = ConnectedAccountsDataProvider.class)
    public void TC_08_negative_Update_ConnectAccount_InvalidParams(String testCaseName, Map<String, Object> body) {
        logger.info("Running invalid update connect account case: {}", testCaseName);

        String connectAccountId = TestContext.getConnectAccountId();
        boolean isFlow = true;
        if (connectAccountId == null) {
            connectAccountId = ConnectedAccountHelper.createConnectAccount(false);
            isFlow = false;
        }

        ConnectAccounts.updateConnectAccount(connectAccountId, body)
                .then()
                .spec(ResponseSpec.bad_request())
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", containsString("Invalid currency"));

        if (!isFlow) {
            connectAccountIdToCleanup.add(connectAccountId);
        }

        logger.info("✅ Correctly rejected invalid update payload: {}", testCaseName);
    }

    // ***************DELETE CONNECT ACCOUNT – NEGATIVE*******************\\

    @Test(groups = {
            "unit" }, dataProvider = "invalidAccountIds", dataProviderClass = ConnectedAccountsDataProvider.class)
    public void TC_09_negative_Delete_ConnectAccount_InvalidId(String invalidAccountId) {
        logger.info("Deleting invalid connect account ID: {}", invalidAccountId);

        ConnectAccounts.deleteConnectAccount(invalidAccountId)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", containsString("No such account"));

        logger.info("✅ Correctly rejected deleting invalid connect account ID: {}", invalidAccountId);
    }

    @Test(groups = { "unit" })
    public void TC_10_negative_Delete_ConnectAccount_AlreadyDeleted() {
        logger.info("Deleting already deleted connect account");

        String connectAccountId = ConnectedAccountHelper.createConnectAccount(false);

        // First deletion succeeds
        ConnectAccounts.deleteConnectAccount(connectAccountId)
                .then()
                .statusCode(200);

        // Second deletion fails with 404 (No such account)
        ConnectAccounts.deleteConnectAccount(connectAccountId)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", containsString("No such account"));

        logger.info("✅ Correctly rejected deleting already deleted connect account");
    }

    @Test
    public void TC_11_positive_Link_Account() {
        String connectAccountId = TestContext.getConnectAccountId();
        Map<String, String> body = new HashMap<>();
        boolean isFlow = true;
        if (connectAccountId == null) {
            connectAccountId = ConnectedAccountHelper.createConnectAccount(false);
            isFlow = false;
        }

        body.put("account", connectAccountId);
        body.put("refresh_url", "https://example.com/reauth");
        body.put("return_url", "https://example.com/return");
        body.put("type", "account_onboarding");

        ConnectAccounts.linkAccount(body)
                .then()
                .statusCode(200)
                .body("object", equalTo("account_link"))
                .body("url", notNullValue());
        if (isFlow == false) {
            connectAccountIdToCleanup.add(connectAccountId);
        }
    }

    @Test
    public void TC_12_negative_Link_Account_Invalid() {
        String connectAccountId = "invalid";
        Map<String, String> body = new HashMap<>();

        body.put("account", connectAccountId);
        body.put("refresh_url", "https://example.com/reauth");
        body.put("return_url", "https://example.com/return");
        body.put("type", "account_onboarding");

        ConnectAccounts.linkAccount(body)
                .then()
                .spec(ResponseSpec.bad_request())
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", containsString("No such account"));
    }

    @Test(groups = { "unit" })
    public void TC_13_positive_Link_Account_Expired_Redirect() {
        String connectAccountId = TestContext.getConnectAccountId();
        boolean isFlow = true;
        if (connectAccountId == null) {
            connectAccountId = ConnectedAccountHelper.createConnectAccount(false);
            isFlow = false;
        }

        Map<String, String> body = new HashMap<>();
        body.put("account", connectAccountId);
        body.put("refresh_url", "https://example.com/reauth");
        body.put("return_url", "https://example.com/return");
        body.put("type", "account_onboarding");

        // 1. Create the Account Link
        String url = ConnectAccounts.linkAccount(body)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("url");

        // 2. First visit to the URL (consumes the single-use link)
        io.restassured.RestAssured.given()
                .redirects().follow(false)
                .when()
                .get(url);

        // 3. Second visit to the same URL (now expired/used)
        // Should redirect (302) to the refresh_url
        io.restassured.RestAssured.given()
                .redirects().follow(false)
                .when()
                .get(url)
                .then()
                .statusCode(302)
                .header("Location", containsString("https://example.com/reauth"));

        if (!isFlow) {
            connectAccountIdToCleanup.add(connectAccountId);
        }
    }

    @AfterMethod
    public void cleanup() {
        for (String connectAccountId : connectAccountIdToCleanup) {
            try {
                ConnectAccounts.deleteConnectAccount(connectAccountId);
            } catch (Exception e) {
                // Ignore cleanup failures
            }
        }
        connectAccountIdToCleanup.clear();
    }

}

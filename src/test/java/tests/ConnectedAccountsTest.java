package tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.github.javafaker.Faker;

import dataprovider.ConnectedAccountsDataProvider;
import endpoints.ConnectAccounts;
import helpers.ConnectedAccountHelper;
import helpers.PojoValidator;
import helpers.TestContext;
import static io.restassured.RestAssured.given;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import models.response.ConnectedAccountResponse;
import specification.ResponseSpec;
import testbase.BaseClass;

public class ConnectedAccountsTest extends BaseClass {

    Faker faker = new Faker();

    List<String> connectAccountIdToCleanup = new ArrayList<>();

    @Test(groups = { "connect_account", "regression", "smoke" })
    public void TC_01_positive_Create_ConnectAccount() {
        logger.info("Testing create connect account");
        Map<String, Object> body = new HashMap<>();
        // body.put("type", "express");
        if (TestContext.getServiceProviderEmail() == null) {
            TestContext.setServiceProviderEmail(faker.internet().safeEmailAddress());
        }
        body.put("email", TestContext.getServiceProviderEmail());

        logger.info("Using service provider email from context: {}", TestContext.getServiceProviderEmail());
        body.put("controller[fees][payer]", "application");
        body.put("controller[losses][payments]", "application");
        body.put("controller[stripe_dashboard][type]", "express");

        Response resp = ConnectAccounts.createConnectAccount(body);
        String connectAccountId = resp.then()
                .body("object", equalTo("account"))
                .body("id", notNullValue())
                .extract()
                .jsonPath()
                .get("id");

        ConnectedAccountResponse caResponse = resp.as(ConnectedAccountResponse.class);
        PojoValidator.validate(caResponse);
        logger.info("POJO validation passed for connected account: {}", connectAccountId);

        logger.info("Created connect account ID: {}", connectAccountId);
        TestContext.setConnectAccountId(connectAccountId);
    }

    @Test(groups = { "connect_account", "regression" })
    public void TC_02_positive_Update_ConnectAccount() {
        logger.info("Testing update connect account");
        String connectAccountId = TestContext.getConnectAccountId();
        if (connectAccountId == null) {
            connectAccountId = ConnectedAccountHelper.createConnectAccount(false);
            logger.info("Created fallback connect account ID: {}", connectAccountId);
            connectAccountIdToCleanup.add(connectAccountId);
        } else {
            logger.info("Using active connect account ID: {}", connectAccountId);
        }
        Map<String, Object> body = new HashMap<>();
        body.put("default_currency", "usd");

        logger.info("Updating connect account ID: {}", connectAccountId);
        Response resp = ConnectAccounts.updateConnectAccount(connectAccountId, body);
        resp.then()
                .body("default_currency", equalTo("usd"))
                .body("id", equalTo(connectAccountId));

        ConnectedAccountResponse caResponse = resp.as(ConnectedAccountResponse.class);
        PojoValidator.validate(caResponse);
        logger.info("POJO validation passed for updated connect account: {}", connectAccountId);
        logger.info("Successfully updated connect account");
    }

    @Test(groups = { "connect_account", "regression" })
    public void TC_03_positive_Delete_ConnectAccount() {
        logger.info("Testing delete connect account");
        String connectAccountId = TestContext.getConnectAccountId();
        if (connectAccountId == null) {
            connectAccountId = ConnectedAccountHelper.createConnectAccount(false);
            logger.info("Created fallback connect account ID: {}", connectAccountId);
        } else {
            logger.info("Using active connect account ID: {}", connectAccountId);
        }

        logger.info("Deleting connect account ID: {}", connectAccountId);
        ConnectAccounts.deleteConnectAccount(connectAccountId)
                .then()
                .body("id", equalTo(connectAccountId));
        logger.info("Successfully deleted connect account");
        TestContext.setConnectAccountId(null);
    }

    @Test(groups = { "connect_account", "regression" })
    public void TC_04_positive_Retrieve_ConnectAccount() {
        logger.info("Testing retrieve connect account");
        String connectAccountId = TestContext.getConnectAccountId();
        if (connectAccountId == null) {
            connectAccountId = ConnectedAccountHelper.createConnectAccount(false);
            connectAccountIdToCleanup.add(connectAccountId);
            logger.info("Created fallback connect account ID: {}", connectAccountId);
        } else {
            logger.info("Using active connect account ID: {}", connectAccountId);
        }

        logger.info("Retrieving connect account ID: {}", connectAccountId);
        Response resp = ConnectAccounts.retrieveConnectAccount(connectAccountId);
        resp.then()
                .body("id", equalTo(connectAccountId));

        ConnectedAccountResponse caResponse = resp.as(ConnectedAccountResponse.class);
        PojoValidator.validate(caResponse);
        logger.info("POJO validation passed for retrieved connect account: {}", connectAccountId);
        logger.info("Successfully retrieved connect account");

    }

    // ***************CREATE CONNECT ACCOUNT – NEGATIVE*******************\\

    @Test(groups = { "connect_account", "negative",
            "regression" }, dataProvider = "invalidConnectAccountPayloads", dataProviderClass = ConnectedAccountsDataProvider.class)
    public void TC_05_negative_Create_ConnectAccount_Invalid(String testCaseName, Map<String, Object> body,String errorParam) {
        logger.info("Running invalid connect account payload case: {}", testCaseName);

        Response response = ConnectAccounts.createConnectAccount(body);

        if (response.getStatusCode() == 200) {
            String connectAccountId = response.jsonPath().getString("id");
            logger.info("Connect account created unexpectedly, adding to cleanup ID: {}", connectAccountId);
            connectAccountIdToCleanup.add(connectAccountId);
        }

        response.then()
                .spec(ResponseSpec.bad_request())
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", notNullValue())
                .body("error.param",equalTo(errorParam));

        logger.info("✅ Correctly rejected invalid connect account payload: {}", testCaseName);
    }

    // ***************RETRIEVE CONNECT ACCOUNT – NEGATIVE*******************\\

    @Test(groups = { "connect_account", "negative",
            "regression" }, dataProvider = "invalidAccountIds", dataProviderClass = ConnectedAccountsDataProvider.class)
    public void TC_06_negative_Retrieve_ConnectAccount_InvalidId(String invalidAccountId,ResponseSpecification errorcode) {
        logger.info("Retrieving invalid connect account ID: {}", invalidAccountId);

        ConnectAccounts.retrieveConnectAccount(invalidAccountId)
                .then()
                .spec(errorcode)
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.code", equalTo("account_invalid"))
                .body("error.message", containsString("does not have access to account"));

        logger.info("✅ Correctly rejected retrieval of invalid connect account ID: {}", invalidAccountId);
    }

    // ***************UPDATE CONNECT ACCOUNT – NEGATIVE*******************\\

    @Test(groups = { "connect_account", "negative", "regression" })
    public void TC_07_negative_Update_ConnectAccount_NonexistentId() {
        logger.info("Updating nonexistent connect account");

        Map<String, Object> body = new HashMap<>();
        body.put("default_currency", "usd");

        ConnectAccounts.updateConnectAccount("acct_invalid_12345", body)
                .then()
                .spec(ResponseSpec.forbidden())
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", containsString("does not have access to account 'acct_invalid_12345' (or that account does not exist). Application access may have been revoked."));

        logger.info("✅ Correctly rejected updating nonexistent connect account");
    }

    @Test(groups = { "connect_account", "negative",
            "regression" }, dataProvider = "invalidUpdatePayloads", dataProviderClass = ConnectedAccountsDataProvider.class)
    public void TC_08_negative_Update_ConnectAccount_InvalidParams(String testCaseName, Map<String, Object> body) {
        logger.info("Running invalid update connect account case: {}", testCaseName);

        String connectAccountId = TestContext.getConnectAccountId();
        if (connectAccountId == null) {
            connectAccountId = ConnectedAccountHelper.createConnectAccount(false);
            connectAccountIdToCleanup.add(connectAccountId);
            logger.info("Created fallback connect account ID: {}", connectAccountId);
        } else {
            logger.info("Using active connect account ID: {}", connectAccountId);
        }

        ConnectAccounts.updateConnectAccount(connectAccountId, body)
                .then()
                .spec(ResponseSpec.bad_request())
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", containsString("Invalid currency"));

        logger.info("✅ Correctly rejected invalid update payload: {}", testCaseName);
    }

    // ***************DELETE CONNECT ACCOUNT – NEGATIVE*******************\\

    @Test(groups = { "connect_account", "negative",
            "regression" }, dataProvider = "invalidAccountIds", dataProviderClass = ConnectedAccountsDataProvider.class)
    public void TC_09_negative_Delete_ConnectAccount_InvalidId(String invalidAccountId,ResponseSpecification errorcode) {
        logger.info("Deleting invalid connect account ID: {}", invalidAccountId);

        ConnectAccounts.deleteConnectAccount(invalidAccountId)
                .then()
                .spec(errorcode)
                .body("error.type", equalTo("invalid_request_error"))
                .body("error.message", containsString("(or that account does not exist)"));

        logger.info("✅ Correctly rejected deleting invalid connect account ID: {}", invalidAccountId);
    }

    @Test(groups = { "connect_account", "negative", "regression" })
    public void TC_10_negative_Delete_ConnectAccount_AlreadyDeleted() {
        logger.info("Deleting already deleted connect account");

        String connectAccountId = ConnectedAccountHelper.createConnectAccount(false);
        logger.info("Created connect account ID: {}", connectAccountId);

        // First deletion succeeds
        logger.info("Performing first deletion of ID: {}", connectAccountId);
        ConnectAccounts.deleteConnectAccount(connectAccountId)
                .then()
                .statusCode(200);
        logger.info("First deletion succeeded");

        // Second deletion fails with 404 (No such account)
        logger.info("Performing second deletion of ID: {}", connectAccountId);
        ConnectAccounts.deleteConnectAccount(connectAccountId)
                .then()
                .spec(ResponseSpec.forbidden())
                .body("error.type", equalTo("api_error"))
                .body("error.code", equalTo("account_invalid"))
                .body("error.message", containsString("account does not exist"));

        logger.info("✅ Correctly rejected deleting already deleted connect account");
    }

    // ***************LINK ACCOUNT – POSITIVE*******************\\
    @Test(groups = { "connect_account", "regression" })
    public void TC_11_positive_Link_Account() {
        logger.info("Testing positive link account");
        String connectAccountId = TestContext.getConnectAccountId();
        Map<String, String> body = new HashMap<>();
        if (connectAccountId == null) {
            connectAccountId = ConnectedAccountHelper.createConnectAccount(false);
            logger.info("Created fallback connect account ID: {}", connectAccountId);
            connectAccountIdToCleanup.add(connectAccountId);
        } else {
            logger.info("Using active connect account ID: {}", connectAccountId);
        }

        body.put("account", connectAccountId);
        body.put("refresh_url", "https://example.com/reauth");
        body.put("return_url", "https://example.com/return");
        body.put("type", "account_onboarding");

        logger.info("Linking account ID: {}", connectAccountId);
        ConnectAccounts.linkAccount(body)
                .then()
                .statusCode(200)
                .body("object", equalTo("account_link"))
                .body("url", notNullValue());
        logger.info("Successfully linked account and verified URL exists");

    }

    // ***************LINK ACCOUNT – NEGATIVE*******************\\
    @Test(groups = { "connect_account", "negative", "regression" })
    public void TC_12_negative_Link_Account_Invalid() {
        logger.info("Testing link account with invalid ID");
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
        logger.info("Successfully verified invalid link account fails");
    }

    @Test(groups = { "connect_account", "regression" })
    public void TC_13_positive_Link_Account_Expired_Redirect() {
        logger.info("Testing link account expired redirect");
        String connectAccountId = ConnectedAccountHelper.createConnectAccount(false);
        logger.info("Created fallback connect account ID: {}", connectAccountId);
        connectAccountIdToCleanup.add(connectAccountId);

        Map<String, String> body = new HashMap<>();
        body.put("account", connectAccountId);
        body.put("refresh_url", "https://example.com/reauth");
        body.put("return_url", "https://example.com/return");
        body.put("type", "account_onboarding");

        // 1. Create the Account Link
        logger.info("Creating account link for ID: {}", connectAccountId);
        String url = ConnectAccounts.linkAccount(body)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("url");
        logger.info("Account link URL: {}", url);

        // 2. First visit to the URL (consumes the single-use link)
        logger.info("Performing first visit to consume link");
        io.restassured.RestAssured.given()
                .redirects().follow(false)
                .when()
                .get(url);

        // 3. Second visit to the same URL (now expired/used)
        // Should redirect (302) to the refresh_url
        logger.info("Performing second visit to check redirect to refresh_url");
        given()
                .redirects().follow(false)
                .when()
                .get(url)
                .then()
                .statusCode(301)
                .header("Location", containsString("https://example.com/reauth"));

        logger.info("Successfully verified link expiration redirects to refresh_url");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        logger.info("Running cleanup for ConnectedAccountsTest");
        logger.info("Deleting {} connect accounts", connectAccountIdToCleanup.size());
        for (String connectAccountId : connectAccountIdToCleanup) {
            try {
                logger.info("Deleting connect account ID: {}", connectAccountId);
                ConnectAccounts.deleteConnectAccount(connectAccountId);
            } catch (Exception e) {
                logger.warn("⚠️ Cleanup failed for connect account: {}", connectAccountId);
            }
        }
        connectAccountIdToCleanup.clear();
    }

}

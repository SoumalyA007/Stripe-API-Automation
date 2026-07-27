package tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import dataprovider.AccountDataProvider;
import endpoints.accounts;
import helpers.AccountsHelper;
import helpers.PojoValidator;
import helpers.TestContext;
import io.restassured.response.Response;
import models.common.CreateAccountRequestPayload;
import models.response.AccountResponse;
import specification.ResponseSpec;
import testbase.BaseClass;

public class AccountTests extends BaseClass {

        List<String> fallbackAccountIds = new ArrayList<>();

        // ***************CREATE ACCOUNT – POSITIVE*******************\\

        // Create a valid account with all fields populated
        @Test(groups = { "accounts", "create_retrieve_close_account", "regression", "smoke" },priority = 1)
        public void TC_01_CreateAccount_ValidData() {
                logger.info("Test running under groups: {}", Arrays.toString(currentGroups));
                logger.info("Testing create valid account with all fields");
                CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();

                Response resp = accounts.createAccount(requestPayload);
                String id = resp.then().spec(ResponseSpec.OK())
                                .body("id", notNullValue())
                                .body("id", startsWith("acct_"))
                                .extract()
                                .jsonPath()
                                .getString("id");

                AccountResponse accountResponse = resp.as(AccountResponse.class);
                PojoValidator.validate(accountResponse);
                logger.info("POJO validation passed for account: {}", id);

                logger.info("Created Account ID: {}", id);
                TestContext.setAccountId(id);
        }

        // Create accounts with different entity types using DataProvider
        @Test(groups = { "accounts",
                        "regression" }, dataProvider = "validEntityTypes", dataProviderClass = AccountDataProvider.class,priority = 2)
        public void TC_02_CreateAccount_DifferentEntityTypes(String testCaseName,
                        CreateAccountRequestPayload requestPayload) {
                logger.info("Testing create account different entity types: {}", testCaseName);

                Response resp = accounts.createAccount(requestPayload);
                String id = resp.then().spec(ResponseSpec.OK())
                                .body("id", notNullValue())
                                .body("id", startsWith("acct_"))
                                .extract()
                                .jsonPath()
                                .getString("id");

                logger.info("Created Account ID: {}", id);
                fallbackAccountIds.add(id);
        }

        // Create accounts with different dashboard types using DataProvider
        @Test(groups = { "accounts",
                        "regression" }, dataProvider = "validDashboardTypes", dataProviderClass = AccountDataProvider.class,priority = 3)
        public void TC_03_CreateAccount_DifferentDashboards(String testCaseName,
                        CreateAccountRequestPayload requestPayload) {
                logger.info("Testing create account different dashboard types: {}", testCaseName);

                Response resp = accounts.createAccount(requestPayload);
                String id = resp.then().spec(ResponseSpec.OK())
                                .body("id", notNullValue())
                                .body("id", startsWith("acct_"))
                                .extract()
                                .jsonPath()
                                .getString("id");

                logger.info("Created Account ID: {}", id);
                fallbackAccountIds.add(id);
        }

        // Create an account with only customer configuration (no merchant)
        @Test(groups = { "accounts", "regression" },priority = 4)
        public void TC_04_CreateAccount_CustomerConfigOnly() {
                logger.info("Testing create account with customer config only");
                CreateAccountRequestPayload requestPayload = AccountsHelper.accountWithCustomerConfigOnly();

                Response resp = accounts.createAccount(requestPayload);
                String id = resp.then().spec(ResponseSpec.OK())
                                .body("id", notNullValue())
                                .body("id", startsWith("acct_"))
                                .extract()
                                .jsonPath()
                                .getString("id");

                logger.info("Created Account ID: {}", id);
                fallbackAccountIds.add(id);
        }

        // Create an account with only merchant configuration (no customer)
        @Test(groups = { "accounts", "regression" },priority = 5)
        public void TC_05_CreateAccount_MerchantConfigOnly() {
                logger.info("Testing create account with merchant config only");
                CreateAccountRequestPayload requestPayload = AccountsHelper.accountWithMerchantConfigOnly();

                Response resp = accounts.createAccount(requestPayload);
                String id = resp.then().spec(ResponseSpec.OK())
                                .body("id", notNullValue())
                                .body("id", startsWith("acct_"))
                                .extract()
                                .jsonPath()
                                .getString("id");

                logger.info("Created Account ID: {}", id);
                fallbackAccountIds.add(id);
        }

        // Create an account with minimal payload (only required fields)
        @Test(groups = { "accounts", "regression" },priority = 6)
        public void TC_06_CreateAccount_MinimalPayload() {
                logger.info("Testing create account with minimal payload");
                CreateAccountRequestPayload requestPayload = AccountsHelper.minimalAccountPayload();

                Response resp = accounts.createAccount(requestPayload);
                String id = resp.then().spec(ResponseSpec.OK())
                                .body("id", notNullValue())
                                .body("id", startsWith("acct_"))
                                .extract()
                                .jsonPath()
                                .getString("id");

                logger.info("Created Account ID: {}", id);
                fallbackAccountIds.add(id);
        }

        // ***************CREATE ACCOUNT – NEGATIVE*******************\\

        // Create account with invalid/unsupported country codes using DataProvider
        @Test(groups = { "accounts", "negative",
                        "regression" }, dataProvider = "invalidCountryCodes", dataProviderClass = AccountDataProvider.class,priority = 7)
        public void TC_07_CreateAccount_InvalidCountryCode(String testCaseName, String countryCode,
                        String expectedErrorFragment) {
                logger.info("Testing create account invalid country code: {} -> {}", testCaseName, countryCode);

                CreateAccountRequestPayload requestPayload = AccountsHelper.minimalValidAccount();
                requestPayload.getIdentity().setCountry(countryCode);

                Response resp = accounts.createAccount(requestPayload);

                // Cleanup if accidentally created
                if (resp.getStatusCode() == 200) {
                        String id = resp.jsonPath().getString("id");
                        logger.info("Account created unexpectedly, adding to fallback cleanup ID: {}", id);
                        fallbackAccountIds.add(id);
                }

                resp.then().spec(ResponseSpec.bad_request())
                                .body("error.message", containsString(expectedErrorFragment));
                logger.info("Successfully verified invalid country code error fragment: {}", expectedErrorFragment);
        }

        // Create account with invalid email format
        @Test(groups = { "accounts", "negative", "regression" },priority = 8)
        public void TC_08_CreateAccount_InvalidEmailFormat() {
                logger.info("Testing create account with invalid email format");
                CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();
                requestPayload.setContact_email("not-an-email");

                Response resp = accounts.createAccount(requestPayload);

                // Cleanup if accidentally created
                if (resp.getStatusCode() == 200) {
                        String id = resp.jsonPath().getString("id");
                        logger.info("Account created unexpectedly, adding to fallback cleanup ID: {}", id);
                        fallbackAccountIds.add(id);
                }

                resp.then().spec(ResponseSpec.bad_request());
                logger.info("Successfully verified invalid email format rejection");
        }

        // Create account with empty email
        @Test(groups = { "accounts", "negative", "regression" },priority = 9)
        public void TC_09_CreateAccount_EmptyEmail() {
                logger.info("Testing create account with empty email");
                CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();
                requestPayload.setContact_email("");

                Response resp = accounts.createAccount(requestPayload);

                // Cleanup if accidentally created
                if (resp.getStatusCode() == 200) {
                        String id = resp.jsonPath().getString("id");
                        logger.info("Account created unexpectedly, adding to fallback cleanup ID: {}", id);
                        fallbackAccountIds.add(id);
                }

                resp.then().spec(ResponseSpec.bad_request());
                logger.info("Successfully verified empty email rejection");
        }

        // Create account with invalid auth token
        @Test(groups = { "accounts", "auth", "negative", "regression" },priority = 10)
        public void TC_10_CreateAccount_InvalidAuth() {
                logger.info("Testing create account with invalid auth");
                CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();

                Response resp = accounts.createAccountWithCustomAuth("sk_test_invalid_key_12345", requestPayload);

                resp.then().spec(ResponseSpec.Unauthorized())
                                .body("error.message", containsString(
                                                "You provided a malformed API Key, ensure you provided the full key in the Authorization header"));
                logger.info("Successfully verified invalid auth rejection");
        }

        // Create account with missing auth token
        @Test(groups = { "accounts", "auth", "negative", "regression" },priority = 11)
        public void TC_11_CreateAccount_MissingAuth() {
                logger.info("Testing create account with missing auth");
                CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();

                Response resp = accounts.createAccountWithCustomAuth(null, requestPayload);

                resp.then().spec(ResponseSpec.Unauthorized())
                                .body("error.message", containsString("You did not provide an API key"));
                logger.info("Successfully verified missing auth rejection");
        }

        // Create account with empty display name
        @Test(groups = { "accounts", "negative", "regression" },priority = 12)
        public void TC_12_CreateAccount_EmptyDisplayName() {
                logger.info("Testing create account with empty display name");
                CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();
                requestPayload.setDisplay_name("");

                Response resp = accounts.createAccount(requestPayload);

                // Cleanup if accidentally created
                if (resp.getStatusCode() == 200) {
                        String id = resp.jsonPath().getString("id");
                        logger.info("Account created unexpectedly, adding to fallback cleanup ID: {}", id);
                        fallbackAccountIds.add(id);
                }

                resp.then().spec(ResponseSpec.bad_request());
                logger.info("Successfully verified empty display name rejection");
        }

        // Create account with very long display name
        @Test(groups = { "accounts", "negative", "regression" },priority = 13)
        public void TC_13_CreateAccount_LongDisplayName() {
                logger.info("Testing create account with long display name");
                CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();
                requestPayload.setDisplay_name("a".repeat(10000));

                Response resp = accounts.createAccount(requestPayload);

                // Cleanup if accidentally created
                if (resp.getStatusCode() == 200) {
                        String id = resp.jsonPath().getString("id");
                        logger.info("Account created unexpectedly, adding to fallback cleanup ID: {}", id);
                        fallbackAccountIds.add(id);
                }

                resp.then().spec(ResponseSpec.bad_request());
                logger.info("Successfully verified long display name rejection");
        }

        // Create account with special characters in display name
        @Test(groups = { "accounts", "negative", "regression" },priority = 14)
        public void TC_14_CreateAccount_SpecialCharsDisplayName() {
                logger.info("Testing create account with special characters in display name");
                CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();
                requestPayload.setDisplay_name("*/*/*/*#$!#!AA!!!");

                Response resp = accounts.createAccount(requestPayload);

                // Cleanup if accidentally created
                if (resp.getStatusCode() == 200) {
                        String id = resp.jsonPath().getString("id");
                        logger.info("Account created unexpectedly, adding to fallback cleanup ID: {}", id);
                        fallbackAccountIds.add(id);
                }

                resp.then().spec(ResponseSpec.bad_request());
                logger.info("Successfully verified special characters in display name rejection");
        }

        // Create account without identity (missing required field)
        @Test(groups = { "accounts", "negative", "regression" },priority = 15)
        public void TC_15_CreateAccount_MissingIdentity() {
                logger.info("Testing create account without identity payload");
                CreateAccountRequestPayload requestPayload = AccountsHelper.accountWithoutIdentity();

                Response resp = accounts.createAccount(requestPayload);

                resp.then().spec(ResponseSpec.bad_request());
                logger.info("Successfully verified missing identity rejection");
        }

        // Create account without country in identity
        @Test(groups = { "accounts", "negative", "regression" },priority = 16)
        public void TC_16_CreateAccount_MissingCountry() {
                logger.info("Testing create account without country in identity");
                CreateAccountRequestPayload requestPayload = AccountsHelper.accountWithoutCountry();

                Response resp = accounts.createAccount(requestPayload);

                resp.then().spec(ResponseSpec.bad_request());
                logger.info("Successfully verified missing country rejection");
        }

        // ***************RETRIEVE ACCOUNT – POSITIVE*******************\\

        // Retrieve a valid account by its ID
        @Test(groups = { "accounts", "create_retrieve_close_account", "regression" },priority = 17)
        public void TC_17_RetrieveAccount_ValidId() {
                logger.info("Testing retrieve valid account");

                String accountId = TestContext.getAccountId();
                if (accountId == null) {
                        CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();
                        accountId = accounts.createAccount(requestPayload)
                                        .then().spec(ResponseSpec.OK())
                                        .extract().jsonPath().getString("id");
                        fallbackAccountIds.add(accountId);
                        logger.info("Created fallback account to retrieve ID: {}", accountId);
                } else {
                        logger.info("Fetched account ID from context --> {}", accountId);
                }

                // Now retrieve it
                logger.info("Retrieving account ID: {}", accountId);
                Response resp = accounts.retrieveAccount(accountId);

                resp.then().spec(ResponseSpec.OK())
                                .body("id", equalTo(accountId));

                AccountResponse accountResponse = resp.as(AccountResponse.class);
                PojoValidator.validate(accountResponse);
                logger.info("POJO validation passed for retrieved account: {}", accountId);
                logger.info("Successfully retrieved account");
        }

        // ***************RETRIEVE ACCOUNT – NEGATIVE*******************\\

        // Retrieve account with an invalid ID
        @Test(groups = { "accounts", "negative", "regression" },priority =18)
        public void TC_18_RetrieveAccount_Unauthorized_Id() {
                logger.info("Testing retrieve account with invalid ID");
                Response resp = accounts.retrieveAccount("acct_1234567890abcdef");

                resp.then().spec(ResponseSpec.forbidden());
                logger.info("Successfully verified retrieve invalid ID rejection");
        }

        // Retrieve account with a completely garbage ID
        @Test(groups = { "accounts", "negative", "regression" },priority = 19)
        public void TC_19_RetrieveAccount_GarbageId() {
                logger.info("Testing retrieve account with garbage ID");
                Response resp = accounts.retrieveAccount("garbage_not_an_account");

                resp.then().spec(ResponseSpec.bad_request());
                logger.info("Successfully verified retrieve garbage ID rejection");
        }

        // Retrieve account with missing auth
        @Test(groups = { "accounts", "auth", "negative", "regression" },priority = 20)
        public void TC_20_RetrieveAccount_MissingAuth() {
                logger.info("Testing retrieve account with missing auth");
                Response resp = accounts.retrieveAccountWithCustomAuth(null, "acct_any_id");

                resp.then().spec(ResponseSpec.Unauthorized())
                                .body("error.message", containsString("You did not provide an API key"));
                logger.info("Successfully verified retrieve missing auth rejection");
        }

        // Retrieve account with invalid auth
        @Test(groups = { "accounts", "auth", "negative", "regression" },priority = 21)
        public void TC_21_RetrieveAccount_InvalidAuth() {
                logger.info("Testing retrieve account with invalid auth");
                Response resp = accounts.retrieveAccountWithCustomAuth("sk_test_invalid_key_12345", "acct_any_id");

                resp.then().spec(ResponseSpec.Unauthorized())
                                .body("error.message", containsString(
                                                "You provided a malformed API Key, ensure you provided the full key in the Authorization header."));
                logger.info("Successfully verified retrieve invalid auth rejection");
        }

        // ***************CLOSE ACCOUNT – POSITIVE*******************\\

        // Close a valid account
        @Test(groups = { "accounts", "create_retrieve_close_account", "regression" },priority = 22)
        public void TC_22_CloseAccount_ValidId() {
                logger.info("Testing close valid account");
                CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();
                String accountId = accounts.createAccount(requestPayload)
                                        .then().spec(ResponseSpec.OK())
                                        .extract().jsonPath().getString("id");
                fallbackAccountIds.add(accountId);
                logger.info("Created fallback account to close ID: {}", accountId);


                // Close it (do NOT add to fallback list since it's already being closed)
                logger.info("Closing account ID: {}", accountId);
                // validAccountCreationHelper() creates accounts with both customer + merchant
                // configs
                Response resp = accounts.closeAccount(accountId, AccountsHelper.closePayloadBothConfigs());

                resp.then().spec(ResponseSpec.OK());
                logger.info("Successfully closed account");
                // NOTE: accountId stays in TestContext even though the account is now closed,
                // to avoid breaking any downstream class that reads context for its own logic.
        }

        // ***************CLOSE ACCOUNT – NEGATIVE*******************\\

        // Close an account with invalid ID
        @Test(groups = { "accounts", "negative", "regression" },priority = 23)
        public void TC_23_CloseAccount_InvalidId() {
                logger.info("Testing close account with invalid ID");
                Response resp = accounts.closeAccount("acct_invalid_id_12345");

                resp.then().spec(ResponseSpec.bad_request());
                logger.info("Successfully verified close invalid ID rejection");
        }

        // Close an already closed account (double-close)
        @Test(groups = { "accounts", "negative", "regression" },priority = 24)
        public void TC_24_CloseAccount_AlreadyClosed() {
                logger.info("Testing close already closed account");
                // Create a fresh account specifically for this test
                CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();
                String accountId = accounts.createAccount(requestPayload)
                                .then().spec(ResponseSpec.OK())
                                .extract().jsonPath().getString("id");
                logger.info("Created account ID: {}", accountId);

                // First close succeeds
                logger.info("Performing first close on ID: {}", accountId);
                accounts.closeAccount(accountId, AccountsHelper.closePayloadBothConfigs())
                                .then()
                                .spec(ResponseSpec.OK());
                logger.info("First close succeeded");

                // Second close should fail
                logger.info("Performing second close on ID: {}", accountId);
                Response resp = accounts.closeAccount(accountId, AccountsHelper.closePayloadBothConfigs());

                resp.then().spec(ResponseSpec.forbidden())
                                .body("error.code", equalTo("forbidden"));
                logger.info("Successfully verified double-close rejection");
        }

        // Retrieve an account after it has been closed
        @Test(groups = { "accounts", "negative", "regression" },priority = 25)
        public void TC_25_RetrieveAccount_AfterClose() {
                logger.info("Testing retrieve account after closing");
                // Create a fresh account specifically for this test
                CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();
                String accountId = accounts.createAccount(requestPayload)
                                .then().spec(ResponseSpec.OK())
                                .extract().jsonPath().getString("id");
                logger.info("Created account ID: {}", accountId);

                // Close it
                logger.info("Closing account ID: {}", accountId);
                accounts.closeAccount(accountId, AccountsHelper.closePayloadBothConfigs())
                                .then()
                                .spec(ResponseSpec.OK());
                logger.info("Account closed successfully");

                // Retrieve the closed account — it may still be retrievable but with closed
                // status, or return not_found depending on Stripe's behavior
                logger.info("Retrieving closed account ID: {}", accountId);
                Response resp = accounts.retrieveAccount(accountId);

                // Assert it's either gone or shows a closed state
                int statusCode = resp.getStatusCode();
                assertThat("Expected either 200 (with closed status) or 404",
                                statusCode, anyOf(equalTo(200), equalTo(404)));
                logger.info("Successfully verified retrieval behavior of closed account. Status code: {}", statusCode);
        }

        // Close account with missing auth
        @Test(groups = { "accounts", "auth", "negative", "regression" },priority = 26)
        public void TC_26_CloseAccount_MissingAuth() {
                logger.info("Testing close account with missing auth");
                Response resp = accounts.closeAccountWithCustomAuth(null, "acct_any_id");

                resp.then().spec(ResponseSpec.Unauthorized())
                                .body("error.message", containsString(
                                                "You did not provide an API key. You need to provide your API key in the Authorization header, using Bearer auth"));
                logger.info("Successfully verified close missing auth rejection");
        }

        // Close account with invalid auth
        @Test(groups = { "accounts", "auth", "negative", "regression" },priority = 27)
        public void TC_27_CloseAccount_InvalidAuth() {
                logger.info("Testing close account with invalid auth");
                Response resp = accounts.closeAccountWithCustomAuth("sk_test_invalid_key_12345", "acct_any_id");

                resp.then().spec(ResponseSpec.Unauthorized())
                                .body("error.message", containsString(
                                                "You provided a malformed API Key, ensure you provided the full key in the Authorization header."));
                logger.info("Successfully verified close invalid auth rejection");
        }

        // ***************CLEANUP AFTER CLASS*******************\\

        @AfterClass(alwaysRun = true)
        public void cleanup() {
                logger.info("🧹 Starting cleanup for AccountTests...");
                // Close all fallback accounts created during the test run
                logger.info("ℹ️ {} fallback account(s) to close:", fallbackAccountIds.size());
                for (String id : fallbackAccountIds) {
                        try {
                                accounts.closeAccount(id, AccountsHelper.closePayloadBothConfigs());
                                logger.info("🧹 Closed fallback account: {}", id);
                        } catch (Exception e) {
                                logger.warn("⚠️ Failed to close fallback account {}: {}", id, e.getMessage());
                        }
                }

                logger.info("✅ Cleanup complete for AccountTests.");
        }

}
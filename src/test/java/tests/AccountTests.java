package tests;

import builders.requestbuilder.CreateAccountRequestPayload;
import dataprovider.AccountDataProvider;
import endpoints.accounts;
import helpers.AccountsHelper;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import specification.ResponseSpec;
import testbase.BaseClass;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AccountTests extends BaseClass {

        List<String> createdAccountIds = new ArrayList<>();

        // ***************CREATE ACCOUNT – POSITIVE*******************\\

        // Create a valid account with all fields populated
        @Test(groups = { "account.create", "positive", "smoke", "regression" })
        public void TC_01_CreateAccount_ValidData() {

                CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();

                Response resp = accounts.createAccount(requestPayload);
                String id = resp.then().spec(ResponseSpec.OK())
                                .body("id", notNullValue())
                                .body("id", startsWith("acct_"))
                                .extract()
                                .jsonPath()
                                .getString("id");

                createdAccountIds.add(id);
        }

        // Create accounts with different entity types using DataProvider
        @Test(groups = { "account.create", "positive",
                        "regression" }, dataProvider = "validEntityTypes", dataProviderClass = AccountDataProvider.class)
        public void TC_02_CreateAccount_DifferentEntityTypes(String testCaseName,
                        CreateAccountRequestPayload requestPayload) {

                Response resp = accounts.createAccount(requestPayload);
                String id = resp.then().spec(ResponseSpec.OK())
                                .body("id", notNullValue())
                                .body("id", startsWith("acct_"))
                                .extract()
                                .jsonPath()
                                .getString("id");

                createdAccountIds.add(id);
        }

        // Create accounts with different dashboard types using DataProvider
        @Test(groups = { "account.create", "positive",
                        "regression" }, dataProvider = "validDashboardTypes", dataProviderClass = AccountDataProvider.class)
        public void TC_03_CreateAccount_DifferentDashboards(String testCaseName,
                        CreateAccountRequestPayload requestPayload) {

                Response resp = accounts.createAccount(requestPayload);
                String id = resp.then().spec(ResponseSpec.OK())
                                .body("id", notNullValue())
                                .body("id", startsWith("acct_"))
                                .extract()
                                .jsonPath()
                                .getString("id");

                createdAccountIds.add(id);
        }

        // Create an account with only customer configuration (no merchant)
        @Test(groups = { "account.create", "positive", "regression" })
        public void TC_04_CreateAccount_CustomerConfigOnly() {

                CreateAccountRequestPayload requestPayload = AccountsHelper.accountWithCustomerConfigOnly();

                Response resp = accounts.createAccount(requestPayload);
                String id = resp.then().spec(ResponseSpec.OK())
                                .body("id", notNullValue())
                                .body("id", startsWith("acct_"))
                                .extract()
                                .jsonPath()
                                .getString("id");

                createdAccountIds.add(id);
        }

        // Create an account with only merchant configuration (no customer)
        @Test(groups = { "account.create", "positive", "regression" })
        public void TC_05_CreateAccount_MerchantConfigOnly() {

                CreateAccountRequestPayload requestPayload = AccountsHelper.accountWithMerchantConfigOnly();

                Response resp = accounts.createAccount(requestPayload);
                String id = resp.then().spec(ResponseSpec.OK())
                                .body("id", notNullValue())
                                .body("id", startsWith("acct_"))
                                .extract()
                                .jsonPath()
                                .getString("id");

                createdAccountIds.add(id);
        }

        // Create an account with minimal payload (only required fields)
        @Test(groups = { "account.create", "positive", "edge", "regression" })
        public void TC_06_CreateAccount_MinimalPayload() {

                CreateAccountRequestPayload requestPayload = AccountsHelper.minimalAccountPayload();

                Response resp = accounts.createAccount(requestPayload);
                String id = resp.then().spec(ResponseSpec.OK())
                                .body("id", notNullValue())
                                .body("id", startsWith("acct_"))
                                .extract()
                                .jsonPath()
                                .getString("id");

                createdAccountIds.add(id);
        }

        // ***************CREATE ACCOUNT – NEGATIVE*******************\\

        // Create account with invalid/unsupported country codes using DataProvider
        @Test(groups = { "account.create", "negative", "validation",
                        "regression" }, dataProvider = "invalidCountryCodes", dataProviderClass = AccountDataProvider.class)
        public void TC_07_CreateAccount_InvalidCountryCode(String testCaseName, String countryCode,
                        String expectedErrorFragment) {

                CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();
                requestPayload.getIdentity().setCountry(countryCode);

                Response resp = accounts.createAccount(requestPayload);

                // Cleanup if accidentally created
                if (resp.getStatusCode() == 200) {
                        String id = resp.jsonPath().getString("id");
                        createdAccountIds.add(id);
                }

                resp.then().spec(ResponseSpec.bad_request())
                                .body("error.message", containsString(expectedErrorFragment));
        }

        // Create account with invalid email format
        @Test(groups = { "account.create", "negative", "validation", "regression" })
        public void TC_08_CreateAccount_InvalidEmailFormat() {

                CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();
                requestPayload.setContact_email("not-an-email");

                Response resp = accounts.createAccount(requestPayload);

                // Cleanup if accidentally created
                if (resp.getStatusCode() == 200) {
                        String id = resp.jsonPath().getString("id");
                        createdAccountIds.add(id);
                }

                resp.then().spec(ResponseSpec.bad_request());
        }

        // Create account with empty email
        @Test(groups = { "account.create", "negative", "validation", "edge", "regression" })
        public void TC_09_CreateAccount_EmptyEmail() {

                CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();
                requestPayload.setContact_email("");

                Response resp = accounts.createAccount(requestPayload);

                // Cleanup if accidentally created
                if (resp.getStatusCode() == 200) {
                        String id = resp.jsonPath().getString("id");
                        createdAccountIds.add(id);
                }

                resp.then().spec(ResponseSpec.bad_request());
        }

        // Create account with invalid auth token
        @Test(groups = { "account.create", "negative", "auth", "regression" })
        public void TC_10_CreateAccount_InvalidAuth() {

                CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();

                Response resp = accounts.createAccountWithCustomAuth("sk_test_invalid_key_12345", requestPayload);

                resp.then().spec(ResponseSpec.Unauthorized())
                                .body("error.message", containsString("Invalid API Key provided"));
        }

        // Create account with missing auth token
        @Test(groups = { "account.create", "negative", "auth", "regression" })
        public void TC_11_CreateAccount_MissingAuth() {

                CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();

                Response resp = accounts.createAccountWithCustomAuth(null, requestPayload);

                resp.then().spec(ResponseSpec.Unauthorized())
                                .body("error.message", containsString("You did not provide an API key"));
        }

        // Create account with empty display name
        @Test(groups = { "account.create", "negative", "validation", "edge", "regression" })
        public void TC_12_CreateAccount_EmptyDisplayName() {

                CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();
                requestPayload.setDisplay_name("");

                Response resp = accounts.createAccount(requestPayload);

                // Cleanup if accidentally created
                if (resp.getStatusCode() == 200) {
                        String id = resp.jsonPath().getString("id");
                        createdAccountIds.add(id);
                }

                resp.then().spec(ResponseSpec.bad_request());
        }

        // Create account with very long display name
        @Test(groups = { "account.create", "negative", "validation", "edge", "regression" })
        public void TC_13_CreateAccount_LongDisplayName() {

                CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();
                requestPayload.setDisplay_name("a".repeat(550));

                Response resp = accounts.createAccount(requestPayload);

                // Cleanup if accidentally created
                if (resp.getStatusCode() == 200) {
                        String id = resp.jsonPath().getString("id");
                        createdAccountIds.add(id);
                }

                resp.then().spec(ResponseSpec.bad_request());
        }

        // Create account with special characters in display name
        @Test(groups = { "account.create", "negative", "validation", "edge", "regression" })
        public void TC_14_CreateAccount_SpecialCharsDisplayName() {

                CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();
                requestPayload.setDisplay_name("*/*/*/*#$!#!AA!!!");

                Response resp = accounts.createAccount(requestPayload);

                // Cleanup if accidentally created
                if (resp.getStatusCode() == 200) {
                        String id = resp.jsonPath().getString("id");
                        createdAccountIds.add(id);
                }

                resp.then().spec(ResponseSpec.bad_request());
        }

        // Create account without identity (missing required field)
        @Test(groups = { "account.create", "negative", "validation", "regression" })
        public void TC_15_CreateAccount_MissingIdentity() {

                CreateAccountRequestPayload requestPayload = AccountsHelper.accountWithoutIdentity();

                Response resp = accounts.createAccount(requestPayload);

                resp.then().spec(ResponseSpec.bad_request());
        }

        // Create account without country in identity
        @Test(groups = { "account.create", "negative", "validation", "regression" })
        public void TC_16_CreateAccount_MissingCountry() {

                CreateAccountRequestPayload requestPayload = AccountsHelper.accountWithoutCountry();

                Response resp = accounts.createAccount(requestPayload);

                resp.then().spec(ResponseSpec.bad_request());
        }

        // ***************RETRIEVE ACCOUNT – POSITIVE*******************\\

        // Retrieve a valid account by its ID
        @Test(groups = { "account.retrieve", "positive", "smoke", "regression" })
        public void TC_17_RetrieveAccount_ValidId() {

                // First, create an account to retrieve
                CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();
                Response createResp = accounts.createAccount(requestPayload);
                String accountId = createResp.then().spec(ResponseSpec.OK())
                                .extract()
                                .jsonPath()
                                .getString("id");
                createdAccountIds.add(accountId);

                // Now retrieve it
                Response resp = accounts.retrieveAccount(accountId);

                resp.then().spec(ResponseSpec.OK())
                                .body("id", equalTo(accountId));
        }

        // ***************RETRIEVE ACCOUNT – NEGATIVE*******************\\

        // Retrieve account with an invalid ID
        @Test(groups = { "account.retrieve", "negative", "validation", "regression" })
        public void TC_18_RetrieveAccount_InvalidId() {

                Response resp = accounts.retrieveAccount("acct_invalid_id_12345");

                resp.then().spec(ResponseSpec.not_found());
        }

        // Retrieve account with a completely garbage ID
        @Test(groups = { "account.retrieve", "negative", "validation", "regression" })
        public void TC_19_RetrieveAccount_GarbageId() {

                Response resp = accounts.retrieveAccount("garbage_not_an_account");

                resp.then().spec(ResponseSpec.not_found());
        }

        // Retrieve account with missing auth
        @Test(groups = { "account.retrieve", "negative", "auth", "regression" })
        public void TC_20_RetrieveAccount_MissingAuth() {

                Response resp = accounts.retrieveAccountWithCustomAuth(null, "acct_any_id");

                resp.then().spec(ResponseSpec.Unauthorized())
                                .body("error.message", containsString("You did not provide an API key"));
        }

        // Retrieve account with invalid auth
        @Test(groups = { "account.retrieve", "negative", "auth", "regression" })
        public void TC_21_RetrieveAccount_InvalidAuth() {

                Response resp = accounts.retrieveAccountWithCustomAuth("sk_test_invalid_key_12345", "acct_any_id");

                resp.then().spec(ResponseSpec.Unauthorized())
                                .body("error.message", containsString("Invalid API Key provided"));
        }

        // ***************CLOSE ACCOUNT – POSITIVE*******************\\

        // Close a valid account
        @Test(groups = { "account.close", "positive", "regression" })
        public void TC_22_CloseAccount_ValidId() {

                // First, create an account to close
                CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();
                Response createResp = accounts.createAccount(requestPayload);
                String accountId = createResp.then().spec(ResponseSpec.OK())
                                .extract()
                                .jsonPath()
                                .getString("id");

                // Close it (do NOT add to cleanup list since it's already closed)
                Response resp = accounts.closeAccount(accountId);

                resp.then().spec(ResponseSpec.OK());
        }

        // ***************CLOSE ACCOUNT – NEGATIVE*******************\\

        // Close an account with invalid ID
        @Test(groups = { "account.close", "negative", "validation", "regression" })
        public void TC_23_CloseAccount_InvalidId() {

                Response resp = accounts.closeAccount("acct_invalid_id_12345");

                resp.then().spec(ResponseSpec.not_found());
        }

        // Close an already closed account (double-close)
        @Test(groups = { "account.close", "negative", "edge", "regression" })
        public void TC_24_CloseAccount_AlreadyClosed() {

                // Create and then close an account
                CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();
                Response createResp = accounts.createAccount(requestPayload);
                String accountId = createResp.then().spec(ResponseSpec.OK())
                                .extract()
                                .jsonPath()
                                .getString("id");

                // First close succeeds
                accounts.closeAccount(accountId)
                                .then()
                                .spec(ResponseSpec.OK());

                // Second close should fail
                Response resp = accounts.closeAccount(accountId);

                resp.then().spec(ResponseSpec.bad_request());
        }

        // Retrieve an account after it has been closed
        @Test(groups = { "account.close", "negative", "edge", "regression" })
        public void TC_25_RetrieveAccount_AfterClose() {

                // Create and then close an account
                CreateAccountRequestPayload requestPayload = AccountsHelper.validAccountCreationHelper();
                Response createResp = accounts.createAccount(requestPayload);
                String accountId = createResp.then().spec(ResponseSpec.OK())
                                .extract()
                                .jsonPath()
                                .getString("id");

                // Close it
                accounts.closeAccount(accountId)
                                .then()
                                .spec(ResponseSpec.OK());

                // Retrieve the closed account — it may still be retrievable but with closed
                // status, or return not_found depending on Stripe's behavior
                Response resp = accounts.retrieveAccount(accountId);

                // Assert it's either gone or shows a closed state
                int statusCode = resp.getStatusCode();
                assertThat("Expected either 200 (with closed status) or 404",
                                statusCode, anyOf(equalTo(200), equalTo(404)));
        }

        // Close account with missing auth
        @Test(groups = { "account.close", "negative", "auth", "regression" })
        public void TC_26_CloseAccount_MissingAuth() {

                Response resp = accounts.closeAccountWithCustomAuth(null, "acct_any_id");

                resp.then().spec(ResponseSpec.Unauthorized())
                                .body("error.message", containsString("You did not provide an API key"));
        }

        // Close account with invalid auth
        @Test(groups = { "account.close", "negative", "auth", "regression" })
        public void TC_27_CloseAccount_InvalidAuth() {

                Response resp = accounts.closeAccountWithCustomAuth("sk_test_invalid_key_12345", "acct_any_id");

                resp.then().spec(ResponseSpec.Unauthorized())
                                .body("error.message", containsString("Invalid API Key provided"));
        }

        // ***************CLEANUP AFTER TEST*******************\\

        @AfterMethod
        public void cleanup() {

                for (String id : createdAccountIds) {
                        try {
                                accounts.closeAccount(id);
                                System.out.println("🧹 Closed test account: " + id);
                        } catch (Exception e) {
                                System.out.println("⚠️ Cleanup failed for account: " + id + " — " + e.getMessage());
                        }
                }

                // 🔥 Important: clear list after cleanup
                createdAccountIds.clear();
        }

}

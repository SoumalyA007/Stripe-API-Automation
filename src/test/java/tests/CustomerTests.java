package tests;

import com.github.javafaker.Faker;
import dataprovider.UpdateCustomerDataProvider;
import endpoints.Customer;
import helpers.CustomersHelper;
import helpers.TestContext;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import specification.ResponseSpec;
import testbase.BaseClass;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CustomerTests extends BaseClass {

    Faker faker = new Faker();
    List<String> customerIds = new ArrayList<>();

    private String getOrSetupCustomer() {
        String customerId = TestContext.getCustomerId();
        if (customerId == null) {
            String name = CustomersHelper.getName();
            String email = faker.internet().safeEmailAddress();
            Response resp = Customer.createCustomer(name, email, null);
            customerId = resp.jsonPath().getString("id");
            TestContext.setCustomerId(customerId);
            TestContext.setBillingEmail(email);
            TestContext.setBillingName(name);
            customerIds.add(customerId);
        }
        return customerId;
    }

    // ***************CREATE CUSTOMER*******************\\

    // Create a Valid Customer
    @Test(groups = { "customer", "regression", "create_update_search_retrieve_delete" })
    public void TC_01_CreateCustomer_ValidData() {

        logger.info("Test running under groups: {}", Arrays.toString(currentGroups));

        String name = CustomersHelper.getName();
        String email = faker.internet().safeEmailAddress();
        logger.info("Customer Name and Email \t" + name + " <--> " + email);

        Response resp = Customer.createCustomer(name, email, null);
        String id = resp.then().spec(ResponseSpec.OK())
                .body("id", notNullValue())
                .body("email", equalTo(email))
                .body("name", equalTo(name))
                .extract()
                .jsonPath()
                .get("id");

        TestContext.setCustomerId(id);
        logger.info("Customer ID of user set in context: \t" + TestContext.getCustomerId());
        TestContext.setBillingName(name);
        TestContext.setBillingEmail(email);
        customerIds.add(id);

    }

    // Creating a customer with no name
    @Test(groups = { "customer", "regression" })
    public void TC_02_CreateCustomer_OnlyEmail() {
        logger.info("Testing create customer with only email");
        String email = faker.internet().safeEmailAddress();
        logger.info("Email: {}", email);
        Response resp = Customer.createCustomer(null, email, null);
        String id = resp.then().spec(ResponseSpec.OK())
                .body("id", notNullValue())
                .body("email", equalTo(email))
                .body("name", equalTo(null))
                .extract()
                .jsonPath()
                .get("id");

        logger.info("Created customer ID: {}", id);
        customerIds.add(id);
    }

    // Create Customer with MetaaData
    @Test(groups = { "customer", "regression" })
    public void createCustomerUsingMetadata() {
        logger.info("Testing create customer using metadata");
        String email = faker.internet().emailAddress();
        String name = CustomersHelper.getName();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("name", name);
        metadata.put("source", "automation");
        logger.info("Name: {}, Email: {}, Metadata: {}", name, email, metadata);

        Response response = Customer.createCustomer(null, email, metadata);
        String id = response.then()
                .spec(ResponseSpec.OK())
                .body("email", equalTo(email))
                .body("metadata.name", equalTo(name))
                .body("metadata.source", equalTo("automation"))
                .extract()
                .jsonPath()
                .get("id");

        logger.info("Created customer ID: {}", id);
        customerIds.add(id);
    }

    // Create Customer with invalid token
    @Test(groups = { "customer", "negative", "regression" })
    public void TC_04_CreateCustomer_InvalidApiKey() {
        logger.info("Testing create customer with invalid API key");
        Response response = Customer.createCustomerWithCustomAuth("invalid", "ABC", "ABC");
        response.then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));
        logger.info("Successfully verified invalid API key rejection");
    }

    // create customer with invalid email format
    @Test(groups = { "customer", "negative", "regression" })
    public void TC_05_CreateCustomer_InvalidEmailFormat() {
        logger.info("Testing create customer with invalid email format");
        String name = "test";
        String email = "abc-def";
        Response response = Customer.createCustomer(name, email, null);
        if (response.statusCode() == 200) {
            String id = response.then().extract().jsonPath().get("id");
            logger.info("Customer created unexpectedly, deleting ID: {}", id);
            Customer.deleteCustomer(id);
        }
        response.then().spec(ResponseSpec.bad_request());
        logger.info("Successfully verified invalid email format rejection");
    }

    // Create Customer with no token
    @Test(groups = { "customer", "negative", "auth", "regression" })
    public void TC_06_CreateCustomer_MissingAuth() {
        logger.info("Testing create customer with missing auth");
        Response response = Customer.createCustomerWithCustomAuth(null, "ABC", "ABC");
        response.then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("You did not provide an API key"));
        logger.info("Successfully verified missing auth rejection");
    }

    // Cretae Customer with duplicate mail
    @Test(groups = { "customer", "regression" })
    public void TC_07_CreateCustomer_DuplicateEmail() {
        logger.info("Testing create customer with duplicate email");
        String name = CustomersHelper.getName();
        String email = faker.internet().safeEmailAddress();
        logger.info("Name: {}, Email: {}", name, email);

        logger.info("Creating first customer");
        Response firstResponse = Customer.createCustomer(name, email, null);
        String firstCustomerId = firstResponse.then().spec(ResponseSpec.OK())
                .body("id", notNullValue())
                .body("email", equalTo(email))
                .body("name", equalTo(name))
                .extract()
                .jsonPath()
                .get("id");
        logger.info("First Customer ID: {}", firstCustomerId);

        logger.info("Creating second customer with duplicate email");
        Response secondResponse = Customer.createCustomer(name, email, null);
        String secondCustomerId = secondResponse.then().spec(ResponseSpec.OK())
                .body("id", notNullValue())
                .body("email", equalTo(email))
                .body("name", equalTo(name))
                .extract()
                .jsonPath()
                .get("id");
        logger.info("Second Customer ID: {}", secondCustomerId);

        // 🔹 🔥 IMPORTANT ASSERTIONS
        assertThat(firstCustomerId, notNullValue());
        assertThat(secondCustomerId, notNullValue());

        // ✅ Core validation: IDs must be different
        assertThat(firstCustomerId, not(equalTo(secondCustomerId)));

        // ✅ Email should be same
        assertThat(
                firstResponse.jsonPath().getString("email"),
                equalTo(secondResponse.jsonPath().getString("email")));

        logger.info("Verified customer IDs are different despite having the same email");

        // 🔹 Cleanup BOTH customers
        customerIds.add(firstCustomerId);
        customerIds.add(secondCustomerId);
    }

    // create a customer with very large name
    @Test(groups = { "customer", "negative", "regression" })
    public void TC_08_CreateCustomer_LongName() {
        logger.info("Testing create customer with very long name");
        String name = "a".repeat(550);
        String email = faker.internet().safeEmailAddress();
        Response response = Customer.createCustomer(name, email, null);
        response.then().spec(ResponseSpec.bad_request());
        logger.info("Successfully verified long name rejection");
    }

    // create a customer name with special characters
    @Test(groups = { "customer", "regression" })
    public void TC_09_CreateCustomer_SpecialCharacters() {
        logger.info("Testing create customer with special characters in name");
        String name = "*/*/*/*#$!#!AA!!!";
        String email = faker.internet().safeEmailAddress();

        Response response = Customer.createCustomer(name, email, null);

        // 🔹 Cleanup FIRST (if accidentally created)
        if (response.getStatusCode() == 200) {
            String id = response.jsonPath().getString("id");
            logger.info("Customer created unexpectedly, adding ID to cleanup: {}", id);
            customerIds.add(id); // handled by @AfterMethod
        }

        // 🔹 Main Assertion (your actual test goal)
        response.then().spec(ResponseSpec.bad_request());
        logger.info("Successfully verified special characters rejection");
    }

    // create a customer with values set as null
    @Test(groups = { "customer", "negative", "regression" })
    public void TC_10_CreateCustomer_EmptyValues() {
        logger.info("Testing create customer with empty values");
        String name = null;
        String email = null;

        Response response = Customer.createCustomer(name, email, null);

        // 🔹 Cleanup FIRST (if accidentally created)
        if (response.getStatusCode() == 200) {
            String id = response.jsonPath().getString("id");
            logger.info("Customer created unexpectedly, adding ID to cleanup: {}", id);
            customerIds.add(id); // handled by @AfterMethod
        }

        // 🔹 Main Assertion (your actual test goal)
        response.then().spec(ResponseSpec.bad_request());
        logger.info("Successfully verified empty values rejection");
    }

    // Update customer with name , email and metadata
    @Test(groups = { "customer", "regression",
            "create_update_search_retrieve_delete" }, dataProvider = "updateDataProvider", dataProviderClass = UpdateCustomerDataProvider.class)
    public void TC_01_UpdateCustomer_Name(String fieldName, String fieldValue, Map<String, String> metadata) {
        logger.info("Testing update customer name: fieldName={}, fieldValue={}, metadata={}", fieldName, fieldValue,
                metadata);
        Response resp = null;
        String customerId = getOrSetupCustomer();
        logger.info("Using Customer ID: {}", customerId);
        if (metadata != null) {
            resp = Customer.updateCustomer(customerId, fieldName, null, metadata);
        } else {
            resp = Customer.updateCustomer(customerId, fieldName, fieldValue, null);
        }

        resp.then().spec(ResponseSpec.OK());
        logger.info("Successfully updated customer");
    }

    // Update customer with invalid customer id
    @Test(groups = { "customer", "negative", "regression" })
    public void TC_02_UpdateCustomer_InvalidId() {
        logger.info("Testing update customer with invalid ID");
        Response resp = null;
        String invalidId = "inavlid_customer_id";

        resp = Customer.updateCustomer(invalidId, "name", "Invalid Test", null);

        int statusCode = resp.getStatusCode();
        if (statusCode == 200) {
            String id = resp.jsonPath().getString("id");
            logger.info("Customer updated unexpectedly, adding ID to cleanup: {}", id);
            customerIds.add(id);
        }

        resp.then().spec(ResponseSpec.not_found());
        logger.info("Successfully verified invalid ID update rejection");
    }

    // Update customer with invalid auth
    @Test(groups = { "customer", "negative", "auth", "regression" })
    public void TC_03_UpdateCustomer_InvalidAuth() {
        logger.info("Testing update customer with invalid auth");
        Response resp = null;
        String customerId = getOrSetupCustomer();
        logger.info("Using Customer ID: {}", customerId);

        resp = Customer.updateCustomerWithCustomAuth("invlid_token", customerId, "name", "Soumalya", null);

        int statusCode = resp.getStatusCode();
        if (statusCode == 200) {
            String id = resp.jsonPath().getString("id");
            logger.info("Customer updated unexpectedly, adding ID to cleanup: {}", id);
            customerIds.add(id);
        }

        resp.then().spec(ResponseSpec.forbidden());
        logger.info("Successfully verified invalid auth update rejection");
    }

    // Update customer with missing auth
    @Test(groups = { "customer", "negative", "auth", "regression" })
    public void TC_04_UpdateCustomer_MissingAuth() {
        logger.info("Testing update customer with missing auth");
        Response resp = null;
        String customerId = getOrSetupCustomer();
        logger.info("Using Customer ID: {}", customerId);

        resp = Customer.updateCustomerWithCustomAuth(null, customerId, "name", "Soumalya", null);

        int statusCode = resp.getStatusCode();
        if (statusCode == 200) {
            String id = resp.jsonPath().getString("id");
            logger.info("Customer updated unexpectedly, adding ID to cleanup: {}", id);
            customerIds.add(id);
        }

        resp.then().spec(ResponseSpec.Unauthorized());
        logger.info("Successfully verified missing auth update rejection");
    }

    // Update deleted Customer
    @Test(groups = { "customer", "negative", "regression" })
    public void TC_05_UpdateCustomer_DeletedCustomer() {
        logger.info("Testing update deleted customer");
        Response resp;
        String customerId = getOrSetupCustomer();
        logger.info("Deleting customer first: {}", customerId);
        CustomersHelper.deleteCustomer(customerId);
        resp = Customer.updateCustomer(customerId, "name", "Invalid Test", null);

        int statusCode = resp.getStatusCode();
        if (statusCode == 200) {
            String id = resp.jsonPath().getString("id");
            logger.info("Customer updated unexpectedly, adding ID to cleanup: {}", id);
            customerIds.add(id);
        }

        resp.then().spec(ResponseSpec.not_found());
        logger.info("Successfully verified update deleted customer rejection");
    }

    // ****************RETRIEVE DATA TEST*****************\\

    // Get data with valid customer Id
    @Test(groups = { "customer", "regression", "create_update_search_retrieve_delete" })
    public void TC_01_RetrieveCustomer_ValidId() {
        logger.info("Testing retrieve customer with valid ID");
        String customerId = getOrSetupCustomer();
        logger.info("Retrieving Customer ID: {}", customerId);
        Response resp = Customer.getCustomer(customerId);

        resp.then().spec(ResponseSpec.OK())
                .body("id", equalTo(customerId));
        logger.info("Successfully retrieved customer data");
    }

    // Get customer with valid Id
    @Test(groups = { "customer", "negative", "regression" })
    public void TC_02_RetrieveCustomer_InvalidId() {
        logger.info("Testing retrieve customer with invalid ID");
        Response resp = Customer.getCustomer("invalid_id");
        resp.then().spec(ResponseSpec.not_found());
        logger.info("Successfully verified retrieve invalid ID rejection");
    }

    // Get Customer data with invalidId
    @Test(groups = { "customer", "negative", "auth", "regression" })
    public void TC_03_RetrieveCustomer_MissingAuth() {
        logger.info("Testing retrieve customer with missing auth");
        String customerId = getOrSetupCustomer();
        logger.info("Using Customer ID: {}", customerId);
        Response resp = Customer.getCustomerWithCustomAuth(null, customerId);
        resp.then().spec(ResponseSpec.Unauthorized());
        logger.info("Successfully verified retrieve missing auth rejection");
    }

    // Get customer with deleted customer's id
    @Test(groups = { "customer", "negative", "regression" })
    public void TC_04_RetrieveCustomer_DeletedCustomer() {
        logger.info("Testing retrieve deleted customer");
        String customerId = getOrSetupCustomer();
        logger.info("Deleting Customer ID: {}", customerId);
        Customer.deleteCustomer(customerId);
        Response resp = Customer.getCustomer(customerId);
        resp.then().spec(ResponseSpec.not_found());
        logger.info("Successfully verified retrieve deleted customer rejection");
    }

    // ****************DELETE CUSTOMER TEST*****************\\

    // Delete valid customer
    @Test(groups = { "customer", "regression", "create_update_search_retrieve_delete" })
    public void TC_01_DeleteCustomer_Valid() {
        logger.info("Testing delete valid customer");
        String customerId = getOrSetupCustomer();
        logger.info("Deleting Customer ID: {}", customerId);
        Customer.deleteCustomer(customerId)
                .then()
                .spec(ResponseSpec.OK());
        logger.info("Successfully deleted customer");
    }

    // Delete invalid customer
    @Test(groups = { "customer", "negative", "regression" })
    public void TC_02_DeleteCustomer_InvalidId() {
        logger.info("Testing delete customer with invalid ID");
        String customerId = "invalid";
        Customer.deleteCustomer(customerId)
                .then()
                .spec(ResponseSpec.not_found());
        logger.info("Successfully verified delete invalid ID rejection");
    }

    // Delete already deleted customer
    @Test(groups = { "customer", "negative", "regression" })
    public void TC_03_DeleteCustomer_AlreadyDeleted() {
        logger.info("Testing delete already deleted customer");
        String customerId = getOrSetupCustomer();
        logger.info("Deleting Customer ID first: {}", customerId);
        Customer.deleteCustomer(customerId);
        logger.info("Deleting Customer ID second time: {}", customerId);
        Customer.deleteCustomer(customerId)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.code", equalTo("resource_missing"));
        logger.info("Successfully verified delete already deleted customer rejection");
    }

    @Test(groups = { "customer", "negative", "auth", "regression" })
    public void TC_04_DeleteCustomer_MissingAuth() {
        logger.info("Testing delete customer with missing auth");
        String customerId = getOrSetupCustomer();
        logger.info("Using Customer ID: {}", customerId);

        Customer.deleteCustomerWithCustomAuth(null, customerId)
                .then()
                .spec(ResponseSpec.Unauthorized());
        logger.info("Successfully verified delete missing auth rejection");
    }

    // ****************LIST CUSTOMER TEST*****************\\

    // default customer list
    @Test(groups = { "customer", "regression" })
    public void TC_01_ListCustomers_Default() {
        logger.info("Testing list customers default");
        Map<String, Object> queryParams = new HashMap<>();

        Customer.listCustomers(queryParams)
                .then()
                .spec(ResponseSpec.OK());
        logger.info("Successfully retrieved customer list");
    }

    // Get the list of only 2 customer
    @Test(groups = { "customer", "regression" })
    public void TC_02_ListCustomers_WithFilter() {
        logger.info("Testing list customers with limit filter");
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("limit", 2);
        Customer.listCustomers(queryParams)
                .then()
                .spec(ResponseSpec.OK())
                .body("data.size()", equalTo(2));
        logger.info("Successfully retrieved filtered customer list");
    }

    // Get the result based on pagination
    @Test(groups = { "customer", "regression" })
    public void TC_03_ListCustomers_WithPagination() {
        logger.info("Testing list customers with pagination starting_after");
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("starting_after", "cus_UNt0BtOK1xydSU");
        Customer.listCustomers(queryParams)
                .then()
                .spec(ResponseSpec.OK())
                .body("data.id", not(hasItem("cus_UOqwXnBZ7zW9BX")));
        logger.info("Successfully verified customer list pagination");
    }

    // Get customerlist with invalid token
    @Test(groups = { "customer", "negative", "auth", "regression" })
    public void TC_04_ListCustomers_WithInvalidToken() {
        logger.info("Testing list customers with invalid token");
        Map<String, Object> queryParams = new HashMap<>();

        Customer.listCustomersWithCustomToken("invalid_token", queryParams)
                .then()
                .spec(ResponseSpec.Unauthorized());
        logger.info("Successfully verified list customers invalid token rejection");
    }

    // ****************SEARCH CUSTOMER TEST*****************\\

    // Search a customer by Email
    @Test(groups = { "customer", "regression", "create_update_search_retrieve_delete" })
    public void TC_01_SearchCustomer_ByEmail() {
        logger.info("Testing search customer by email");
        Customer.searchCustomer("email:'furever@example.com'")
                .then()
                .spec(ResponseSpec.OK())
                .body("data.email", everyItem(equalTo("furever@example.com")));
        logger.info("Successfully verified search customer by email");
    }

    // Search a customer by nonexisting email
    @Test(groups = { "customer", "negative", "regression" })
    public void TC_02_SearchCustomer_ByInvalidEmail() {
        logger.info("Testing search customer by non-existing email");
        Customer.searchCustomer("email:'nullll'")
                .then()
                .spec(ResponseSpec.OK())
                .body("data.size()", equalTo(0));
        logger.info("Successfully verified non-existing email search returns empty");
    }

    // Search a customer by valid email with Invalid Query Syntax
    @Test(groups = { "customer", "negative", "regression" })
    public void TC_03_SearchCustomer_ByInvalidQuerySyntax() {
        logger.info("Testing search customer with invalid query syntax");
        Customer.searchCustomer("email->'furever@example.com'")
                .then()
                .spec(ResponseSpec.bad_request());
        logger.info("Successfully verified invalid query syntax rejection");
    }

    // Search a customer by valid email with Invalid Token
    @Test(groups = { "customer", "negative", "auth", "regression" })
    public void TC_04_SearchCustomer_ByInvalidToken() {
        logger.info("Testing search customer with invalid token");
        Customer.searchCustomer("email:'furever@example.com'")
                .then()
                .spec(ResponseSpec.Unauthorized());
        logger.info("Successfully verified search customer invalid token rejection");
    }

    @Test(groups = { "customer", "regression" })
    public void TC_11_positive_Idempotent_CreateCustomer() {
        logger.info("Testing idempotent create customer");
        String name = "Idempotent Cust";
        String email = "idempotent_" + System.currentTimeMillis() + "@example.com";
        logger.info("Name: {}, Email: {}", name, email);

        Map<String, String> headers = new HashMap<>();
        String idempotencyKey = "cust_key_" + System.currentTimeMillis();
        headers.put("Idempotency-Key", idempotencyKey);
        logger.info("Using idempotency key: {}", idempotencyKey);

        Response firstResponse = Customer.createCustomer(name, email, null, headers)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .response();

        String firstCustomerId = firstResponse.jsonPath().getString("id");
        logger.info("First response Customer ID: {}", firstCustomerId);

        Response secondResponse = Customer.createCustomer(name, email, null, headers)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .response();

        String secondCustomerId = secondResponse.jsonPath().getString("id");
        logger.info("Second response Customer ID: {}", secondCustomerId);

        org.testng.Assert.assertEquals(firstCustomerId, secondCustomerId);
        logger.info("Verified customer IDs are equal (Idempotency success)");

        if (firstCustomerId != null) {
            customerIds.add(firstCustomerId);
        }
    }

    // ****************CLEANUP AFTER TEST*****************\\
    @AfterMethod
    public void cleanup(org.testng.ITestContext testContext) {
        logger.info("Running cleanup after test method");
        boolean isFlow = false;
        if (testContext.getIncludedGroups() != null) {
            for (String group : testContext.getIncludedGroups()) {
                if ("flow".equals(group)) {
                    isFlow = true;
                    break;
                }
            }
        }
        if (isFlow) {
            logger.info("In flow mode, bypassing method-level cleanup (delegated to suite cleanup)");
            return; // In flow mode, let @AfterSuite handle deletion
        }

        logger.info("Cleaning up {} created customers", customerIds.size());
        for (String id : customerIds) {
            try {
                logger.info("Deleting customer ID: {}", id);
                Customer.deleteCustomer(id);
            } catch (Exception e) {
                logger.error("Cleanup failed for customer: {}", id, e);
            }
        }

        // 🔥 Important: clear list after cleanup
        customerIds.clear();
    }

}
# 🧩 Adding New Tests — Contributor Guide

This guide walks you through adding a new Stripe resource to the framework from scratch, following the exact patterns already used throughout the codebase.

---

## Checklist for a New Stripe Resource

```
□ 1. Create the Endpoint class          endpoints/MyResource.java
□ 2. Create the POJO                    models/response/MyResourceResponse.java
□ 3. Create the Helper                  helpers/MyResourceHelper.java
□ 4. Create the Data Provider           dataprovider/MyResourceDataProvider.java
□ 5. Create the Test class              tests/MyResourceTests.java
□ 6. Add TestContext fields             helpers/TestContext.java
□ 7. Create a TestNG suite XML          testng-myresource.xml
□ 8. Add to regression XML files        testng-regression.xml, testng.xml
```

---

## Step 1 — Endpoint Class

```java
package endpoints;

import io.restassured.response.Response;
import specification.RequestSpec;
import java.util.Map;
import static io.restassured.RestAssured.given;
import static testbase.BaseClass.p;

public class MyResource {

    // ============== CREATE ==============
    public static Response createMyResource(Map<String, Object> body) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/my_resources")
                .formParams(body)
                .when()
                .post();
    }

    // ============== RETRIEVE ==============
    public static Response retrieveMyResource(String id) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/my_resources/{id}")
                .pathParam("id", id)
                .when()
                .get();
    }

    // ============== UPDATE ==============
    public static Response updateMyResource(String id, Map<String, Object> body) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/my_resources/{id}")
                .pathParam("id", id)
                .formParams(body)
                .when()
                .post();
    }

    // ============== DELETE ==============
    public static Response deleteMyResource(String id) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/my_resources/{id}")
                .pathParam("id", id)
                .when()
                .delete();
    }

    // ============== CUSTOM AUTH (for auth failure tests) ==============
    public static Response createMyResourceWithCustomAuth(String token, Map<String, Object> body) {
        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v1/my_resources")
                .contentType("application/x-www-form-urlencoded");
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return request.formParams(body).when().post();
    }
}
```

---

## Step 2 — POJO

```java
package models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO representing the Stripe MyResource API response.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MyResourceResponse {

    @NotNull(message = "id must not be null")
    @Pattern(regexp = "^myres_.*", message = "id must start with 'myres_'")
    @JsonProperty("id")
    private String id;

    @NotBlank(message = "'object' field must not be blank")
    @JsonProperty("object")
    private String object;

    @NotBlank(message = "'status' must not be blank")
    @JsonProperty("status")
    private String status;

    @NotNull(message = "'created' timestamp must not be null")
    @JsonProperty("created")
    private Long created;

    @NotNull(message = "'livemode' must not be null")
    @JsonProperty("livemode")
    private Boolean livemode;

    // Add optional fields without validation annotations
    @JsonProperty("description")
    private String description;
}
```

> **Validation constraint guide:**
> - `@NotNull` — field must be present (not `null`)
> - `@NotBlank` — string must not be null or empty
> - `@Pattern` — string must match the regex
> - `@Min` — numeric value must be >= the given minimum
> - No annotation = optional field (present or absent is fine)

---

## Step 3 — Helper

```java
package helpers;

import endpoints.MyResource;
import specification.ResponseSpec;
import testbase.BaseClass;
import java.util.HashMap;
import java.util.Map;

public class MyResourceHelper {

    /**
     * Creates a fallback MyResource.
     * Called when TestContext.getMyResourceId() returns null (standalone run).
     *
     * @return the created resource ID
     */
    public static String createFallbackMyResource() {
        // Ensure a customer exists if required
        String customerId = TestContext.getCustomerId();
        if (customerId == null) {
            customerId = CustomersHelper.createCustomer();
            TestContext.setCustomerId(customerId);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("customer", customerId);
        body.put("amount", BaseClass.amount);
        body.put("currency", "usd");

        return MyResource.createMyResource(body)
                .then()
                .spec(ResponseSpec.OK())
                .extract()
                .jsonPath()
                .getString("id");
    }
}
```

---

## Step 4 — Data Provider

```java
package dataprovider;

import org.testng.annotations.DataProvider;
import java.util.HashMap;
import java.util.Map;

public class MyResourceDataProvider {

    @DataProvider(name = "invalidMyResourcePayloads")
    public Object[][] invalidMyResourcePayloads() {
        Map<String, Object> missingRequired = new HashMap<>();
        // intentionally empty or missing required fields

        Map<String, Object> invalidCurrency = new HashMap<>();
        invalidCurrency.put("currency", "XYZ");

        return new Object[][] {
                { "Missing required field", missingRequired },
                { "Invalid currency", invalidCurrency },
        };
    }

    @DataProvider(name = "invalidMyResourceIds")
    public Object[][] invalidMyResourceIds() {
        return new Object[][] {
                { "Random string", "myres_invalid_12345", "No such" },
                { "Wrong prefix",  "ch_not_my_resource",  "No such" },
        };
    }
}
```

---

## Step 5 — Test Class

```java
package tests;

import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataprovider.MyResourceDataProvider;
import endpoints.MyResource;
import helpers.MyResourceHelper;
import helpers.PojoValidator;
import helpers.TestContext;
import io.restassured.response.Response;
import models.response.MyResourceResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import specification.ResponseSpec;
import testbase.BaseClass;

public class MyResourceTests extends BaseClass {

    List<String> fallbackResourceIds = new ArrayList<>();

    // ── POSITIVE ─────────────────────────────────────────────────────────────

    @Test(groups = { "myresource", "regression" })
    public void TC_01_Create_Valid_MyResource() {
        logger.info("Testing create valid MyResource");

        // Fallback pattern: use context ID if available, else create fresh
        String customerId = TestContext.getCustomerId();
        if (customerId == null) {
            customerId = helpers.CustomersHelper.createCustomer();
            TestContext.setCustomerId(customerId);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("customer", customerId);
        body.put("amount", amount);
        body.put("currency", "usd");

        Response resp = MyResource.createMyResource(body);
        String id = resp.then()
                .spec(ResponseSpec.OK())
                .body("object", equalTo("my_resource"))
                .body("status", equalTo("active"))
                .extract().jsonPath().getString("id");

        // POJO validation
        MyResourceResponse pojo = resp.as(MyResourceResponse.class);
        PojoValidator.validate(pojo);
        logger.info("POJO validation passed for: {}", id);

        TestContext.setMyResourceId(id);
        logger.info("Created MyResource: {}", id);
    }

    @Test(groups = { "myresource", "regression" },
          dependsOnMethods = "TC_01_Create_Valid_MyResource")
    public void TC_02_Retrieve_MyResource() {
        logger.info("Testing retrieve MyResource");

        String id = TestContext.getMyResourceId();
        if (id == null) {
            id = MyResourceHelper.createFallbackMyResource();
            fallbackResourceIds.add(id);
        }

        Response resp = MyResource.retrieveMyResource(id);
        resp.then()
                .spec(ResponseSpec.OK())
                .body("id", equalTo(id))
                .body("object", equalTo("my_resource"));

        MyResourceResponse pojo = resp.as(MyResourceResponse.class);
        PojoValidator.validate(pojo);
        logger.info("Retrieved MyResource: {}", id);
    }

    // ── NEGATIVE ─────────────────────────────────────────────────────────────

    @Test(groups = { "myresource", "negative", "regression" },
          dataProvider = "invalidMyResourcePayloads",
          dataProviderClass = MyResourceDataProvider.class)
    public void TC_10_Create_Invalid_Payloads(String caseName, Map<String, Object> body) {
        logger.info("Testing invalid create: {}", caseName);
        MyResource.createMyResource(body)
                .then()
                .spec(ResponseSpec.bad_request());
    }

    @Test(groups = { "myresource", "negative", "regression" },
          dataProvider = "invalidMyResourceIds",
          dataProviderClass = MyResourceDataProvider.class)
    public void TC_11_Retrieve_Invalid_Ids(String caseName, String id, String expectedError) {
        logger.info("Testing invalid retrieve: {}", caseName);
        MyResource.retrieveMyResource(id)
                .then()
                .spec(ResponseSpec.not_found())
                .body("error.message", containsString(expectedError));
    }

    // ── AUTH ──────────────────────────────────────────────────────────────────

    @Test(groups = { "myresource", "negative", "auth", "regression" })
    public void TC_15_Create_Invalid_Auth() {
        MyResource.createMyResourceWithCustomAuth("sk_test_invalid_key", Map.of())
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("Invalid API Key provided"));
    }

    @Test(groups = { "myresource", "negative", "auth", "regression" })
    public void TC_16_Create_Missing_Auth() {
        MyResource.createMyResourceWithCustomAuth(null, Map.of())
                .then()
                .spec(ResponseSpec.Unauthorized())
                .body("error.message", containsString("You did not provide an API key"));
    }

    // ── CLEANUP ───────────────────────────────────────────────────────────────

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        logger.info("Cleaning up {} fallback resource(s)", fallbackResourceIds.size());
        for (String id : fallbackResourceIds) {
            try {
                MyResource.deleteMyResource(id);
                logger.info("Deleted: {}", id);
            } catch (Exception e) {
                logger.warn("Could not delete {} — skipping", id);
            }
        }
        fallbackResourceIds.clear();
    }
}
```

---

## Step 6 — Add to TestContext

Open `helpers/TestContext.java` and add:

```java
// At the bottom with other ThreadLocal declarations:
private static final ThreadLocal<String> myResourceId = new ThreadLocal<>();

// Getter and setter (with the other getters/setters):
public static void setMyResourceId(String id) {
    TestContext.myResourceId.set(id);
}

public static String getMyResourceId() {
    return myResourceId.get();
}

// In the clear() method:
myResourceId.remove();
```

---

## Step 7 — Create TestNG Suite XML

`testng-myresource.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">
<suite name="MyResource Suite" verbose="2">
    <listeners>
        <listener class-name="listeners.SuiteCleanupListener"/>
    </listeners>
    <test name="MyResource Tests" preserve-order="true">
        <groups>
            <run>
                <include name="myresource"/>
            </run>
        </groups>
        <classes>
            <class name="tests.MyResourceTests"/>
        </classes>
    </test>
</suite>
```

---

## Step 8 — Register in Regression Suites

Add `<class name="tests.MyResourceTests"/>` to:
- `testng-regression.xml`
- `testng.xml` (regression block)

---

## Coding Standards

| Rule | Example |
|---|---|
| Log every test entry | `logger.info("Testing create valid MyResource")` |
| Use fallback pattern | `if (id == null) { id = MyResourceHelper.createFallbackMyResource(); }` |
| Track fallback IDs | `fallbackResourceIds.add(id)` |
| Validate POJOs | `PojoValidator.validate(resp.as(MyResourceResponse.class))` |
| Keep assertions close to API call | Chain `.body(...)` on the same `.then()` block |
| Group every test | `groups = { "myresource", "regression" }` |
| Clean up in `@AfterClass` | Try-catch per deletion, log skips |

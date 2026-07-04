# 🏗️ Framework Architecture

This document explains the internal design of the Stripe API Automation framework — the layers, patterns, and how the pieces fit together.

---

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                          TEST LAYER                                  │
│  CustomerTests  PaymentIntentTests  InvoicesTest  DisputesTest ...  │
│  (14 test classes, 150+ @Test methods)                              │
└───────────────────────────┬─────────────────────────────────────────┘
                            │  uses
┌───────────────────────────▼─────────────────────────────────────────┐
│                        HELPER LAYER                                  │
│  PaymentIntentHelper  InvoicesHelper  TransfersHelper  TestContext  │
│  RefundHelper  PojoValidator  CustomersHelper  ...                  │
└──────────┬────────────────┬────────────────────────────────────────┘
           │ uses           │ shares state via
┌──────────▼────────┐  ┌────▼──────────────────────────────────────┐
│   ENDPOINT LAYER  │  │           DATA PROVIDERS                   │
│  PaymentIntent    │  │  PaymentIntentDataProvider                 │
│  Invoices         │  │  RefundDataProvider                        │
│  Transfers        │  │  SubscriptionDataProvider  ...             │
│  Payouts          │  └────────────────────────────────────────────┘
│  Refunds ...      │
└──────────┬────────┘
           │ uses
┌──────────▼────────────────────────────────────────────────────────┐
│                    SPECIFICATION LAYER                              │
│         RequestSpec (setupv1 / setupv2)  ·  ResponseSpec           │
│  – Sets Authorization header, base URI, Content-Type, logging      │
└──────────┬────────────────────────────────────────────────────────┘
           │ reads config from
┌──────────▼────────────────────────────────────────────────────────┐
│                      BASE LAYER                                     │
│  BaseClass – config.properties / env vars, Faker, BeforeMethod     │
│  log4j2.xml – rolling log configuration                            │
│  config.properties – credentials (gitignored)                      │
└───────────────────────────────────────────────────────────────────┘
```

---

## Layer-by-Layer Breakdown

### 1. Base Layer — `testbase/BaseClass.java`

The foundation that every test class extends.

```
BaseClass
├── Loads config.properties (local dev)
│     └── Falls back to ENV variables (CI / Jenkins)
├── Exposes: Properties p, Logger logger, Faker faker, int amount
├── @BeforeSuite  – logs suite start
├── @BeforeMethod – captures test groups into currentGroups[]
└── @AfterSuite   – logs suite end
```

**Config resolution priority:**
```
config.properties (local)  ──▶  STRIPE_AUTH_KEY env var (CI)
       ↓ found                          ↓ not found → RuntimeException
  Load all properties            Map env vars to Properties object
```

---

### 2. Specification Layer — `specification/`

Reusable REST Assured specifications shared across all endpoint classes.

| Class | Purpose |
|---|---|
| `RequestSpec.setupv1()` | Form-encoded requests (`application/x-www-form-urlencoded`) — used for most Stripe endpoints |
| `RequestSpec.setupv2()` | JSON requests (`application/json`) — used for newer endpoints |
| `ResponseSpec.OK()` | Asserts HTTP 200 |
| `ResponseSpec.bad_request()` | Asserts HTTP 400 |
| `ResponseSpec.not_found()` | Asserts HTTP 404 |
| `ResponseSpec.Unauthorized()` | Asserts HTTP 401 |

Both request specs automatically add:
- `Authorization: Bearer <authKey>`
- Base URI from config
- Request + Response logging filters

---

### 3. Endpoint Layer — `endpoints/`

One class per Stripe API resource. Each method wraps a single HTTP call.

```
endpoints/
├── Customer.java          – /v1/customers
├── PaymentIntent.java     – /v1/payment_intents
├── paymentMethods.java    – /v1/payment_methods
├── SetupIntent.java       – /v1/setup_intents
├── Subscription.java      – /v1/subscriptions
├── Invoices.java          – /v1/invoices + /v1/invoiceitems
├── Transfers.java         – /v1/transfers
├── Payouts.java           – /v1/payouts
├── Refunds.java           – /v1/refunds
├── Disputes.java          – /v1/disputes
├── Events.java            – /v1/events
├── Radar.java             – /v1/radar/early_fraud_warnings
├── ConnectAccounts.java   – /v1/accounts (connected)
├── accounts.java          – /v1/accounts (platform)
├── Price.java             – /v1/prices
└── Product.java           – /v1/products
```

**Method naming pattern** within each endpoint class:

```java
createXxx(body)                   // POST to resource root
retrieveXxx(id)                   // GET  /resource/:id
updateXxx(id, body)               // POST /resource/:id  (Stripe uses POST for update)
deleteXxx(id)                     // DELETE /resource/:id
actionXxx(id)                     // POST /resource/:id/action  (finalize, pay, void…)
createXxxWithCustomAuth(token, …) // Same as above but with a custom/invalid auth key
```

---

### 4. Helper Layer — `helpers/`

Two types of helpers:

#### A. Fallback Helpers (prefixed `createFallback…`)
Create pre-requisite objects when `TestContext` doesn't have them — enabling tests to run both standalone AND as part of a suite.

```
Helper Class              Fallback Methods
─────────────────────────────────────────────────────────────────────
CustomersHelper           createCustomer()
PaymentMethodsHelper      createValidPaymentMethod(saveToContext)
PaymentIntentHelper       createFallbackPaymentIntent(confirm)
                          createCancelledPaymentIntent()
InvoicesHelper            createFallbackDraftInvoice()
                          createFallbackOpenInvoice()
                          createFallbackPaidInvoice()
TransfersHelper           createFallbackTransfer()        ← uses source_transaction
PayoutsHelper             createFallbackPayout()          ← funds platform balance first
RefundHelper              createFallbackRefund()
                          createCancelledRefund()
SetupIntentHelper         createFallbackSetupIntent()
SubscriptionHelper        createFallbackSubscription()
DisputesHelper            createFallbackDispute()
RadarHelper               createFallbackEarlyFraudWarning()
ConnectedAccountHelper    createConnectAccount()
```

#### B. Utility Helpers

| Helper | Purpose |
|---|---|
| `TestContext` | Thread-safe `ThreadLocal` store for sharing IDs between tests in a suite |
| `PojoValidator` | Validates deserialized response POJOs using Jakarta Bean Validation |
| `NegativeTestHelper` | Shared utilities for negative test patterns |

---

### 5. TestContext — Shared State Management

```
TestContext (ThreadLocal<String> per field)
│
├── customerId          ← set by CustomerTests.TC_01
├── paymentMethodId     ← set by PaymentMethodTests
├── paymentIntentId     ← set by PaymentIntentTests.TC_01
├── setupIntentId       ← set by SetupIntentTests
├── invoiceId           ← set by InvoicesTest.TC_01
├── refundId            ← set by RefundTests.TC_01
├── transferId          ← set by TransferTests.TC_01
├── payoutId            ← set by PayoutsTest.TC_01
├── disputeId           ← set by DisputesTest
├── subscriptionId      ← set by SubscriptionTests
├── chargeId            ← set by charge-related flows
└── ... (18 fields total)
```

**How it enables E2E flows:**

```
Test A sets: TestContext.setPaymentIntentId("pi_xxx")
                              ↓
Test B reads: TestContext.getPaymentIntentId()
              ├── "pi_xxx" found → uses it (E2E mode)
              └── null → calls PaymentIntentHelper.createFallbackPaymentIntent()
                                (standalone mode)
```

---

### 6. Data Providers — `dataprovider/`

TestNG `@DataProvider` classes that supply parameterised test data, primarily for negative and edge-case tests.

```
DataProvider Class               Used by Test Class
──────────────────────────────────────────────────────────────
PaymentIntentDataProvider    →   PaymentIntentTests
PaymentMethodsDataProvider   →   PaymentMethodTests
RefundDataProvider           →   RefundTests
SetupIntentDataProvider      →   SetupIntentTests
SubscriptionDataProvider     →   SubscriptionTests
InvoicesDataProvider         →   InvoicesTest
TransfersDataProvider        →   TransferTests
PayoutsDataProvider          →   PayoutsTest
AccountDataProvider          →   AccountTests
DisputesDataProvider         →   DisputesTest
RadarDataProvider            →   RadarTest
ConnectedAccountsDataProvider→   ConnectedAccountsTest
UpdateCustomerDataProvider   →   CustomerTests
```

---

### 7. POJO Layer — `models/response/`

Each Stripe resource has a matching POJO. They are:
- Annotated with `@Data` and `@NoArgsConstructor` (Lombok)
- Annotated with `@JsonIgnoreProperties(ignoreUnknown = true)` (Jackson)
- Validated using `PojoValidator.validate(pojo)` after every successful API call

```java
// Pattern used in every positive test:
Response resp = Invoices.createInvoice(body);
String invoiceId = resp.then()
        .spec(ResponseSpec.OK())
        .body("status", equalTo("draft"))
        .extract().jsonPath().getString("id");

InvoiceResponse pojo = resp.as(InvoiceResponse.class);
PojoValidator.validate(pojo);   // ← throws AssertionError if constraints fail
```

---

### 8. Listeners — `listeners/`

#### `ExtentReportListener` (implements `ITestListener`)
- Creates a timestamped HTML report: `reports/ExtentReport_yyyy-MM-dd_HH-mm-ss.html`
- Archives the previous run's report into `reports/archive/` as a `.zip`
- Marks tests PASS/FAIL/SKIP with full details

#### `SuiteCleanupListener` (implements `ISuiteListener`)
- Fires `onFinish` after every TestNG XML suite
- Cleans up any Stripe test objects created during the run
- Calls `TestContext.clear()` to reset shared state between suites

---

## Design Patterns Used

| Pattern | Where |
|---|---|
| **Builder** | `RequestSpecBuilder` in `RequestSpec` |
| **Factory Method** | `createFallback…()` in all helper classes |
| **Singleton** | `TestContext` (ThreadLocal per thread) |
| **Template Method** | `BaseClass` defines hook methods; test classes override |
| **Fluent Interface** | REST Assured chain: `.given().spec().body().when().post().then().spec().extract()` |
| **Strategy** | Multiple `ResponseSpec` methods (`OK()`, `bad_request()`, etc.) |

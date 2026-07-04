# 🔄 End-to-End Flow Diagrams

This document illustrates all the major end-to-end test flows in the framework.

---

## 1. Marketplace E2E Flow

**Suite:** `testng-marketplace-e2e.xml` | **Group:** `marketplace_e2e`

This is the primary flow simulating a full marketplace payment — from customer creation through payment, transfer to a connected merchant, payout, and refund verification via webhooks.

```mermaid
flowchart TD
    A([🚀 Suite Start]) --> B

    B["👤 Step 1 — Create Customer\nCustomerTests.TC_01\n→ TestContext.setCustomerId()"]
    B --> C

    C["💳 Step 2 — Create Payment Method\nPaymentMethodTests.TC_01\n→ TestContext.setPaymentMethodId()\nCard: tok_bypassPending"]
    C --> D

    D["📎 Step 3 — Attach Payment Method to Customer\nPaymentMethodTests.TC_02\n→ Links PM to customer"]
    D --> E

    E["🔧 Step 4 — Create SetupIntent\nSetupIntentTests.TC_01\n→ TestContext.setSetupIntentId()\n→ Saves card for future off-session use"]
    E --> F

    F["💰 Step 5 — Create PaymentIntent\nPaymentIntentTests.TC_01\n→ TestContext.setPaymentIntentId()"]
    F --> G

    G["✅ Step 6 — Confirm PaymentIntent\nPaymentIntentTests.TC_02\n→ Charge succeeds instantly (tok_bypassPending)\n→ Funds added to platform available balance"]
    G --> H

    H["🔍 Step 7 — Retrieve Charge\nSetupIntentTests.TC_15\n→ Verifies latest_charge on PI"]
    H --> I

    I["💸 Step 8 — Create Transfer\nTransferTests.TC_01\n→ source_transaction = latest_charge\n→ Funds move to connected account\n→ TestContext.setTransferId()"]
    I --> J

    J["🏧 Step 9 — Create Payout\nPayoutsTest.TC_01\n→ On connected account (Stripe-Account header)\n→ TestContext.setPayoutId()"]
    J --> K

    K["🔙 Step 10 — Create Refund\nRefundTests.TC_01 (partial)\n→ Refunds half the charged amount\n→ TestContext.setRefundId()"]
    K --> L

    L["🔔 Step 11 — Verify Webhook Events\nWebhookEventTests\n→ Asserts charge.refunded event\n→ Validates event object fields"]
    L --> M([✅ Suite Complete])

    style A fill:#6B21A8,color:#fff
    style M fill:#166534,color:#fff
    style G fill:#1D4ED8,color:#fff
    style I fill:#92400E,color:#fff
```

---

## 2. Subscription Billing E2E Flow

**Suite:** `testng-subscription-e2e.xml` | **Group:** `subscription_e2e`

```mermaid
flowchart TD
    A([🚀 Start]) --> B
    B["👤 Create Customer\nCustomerTests.TC_01"] --> C
    C["💳 Create & Attach Payment Method\nPaymentMethodTests.TC_01 + TC_02"] --> D
    D["📦 Create Product\nSubscriptionTests — product setup"] --> E
    E["💲 Create Price\n(recurring, monthly)"] --> F
    F["📋 Create Subscription\nSubscriptionTests.TC_01\n→ Links customer + price\n→ Auto-creates first Invoice\n→ TestContext.setSubscriptionId()"] --> G
    G["🧾 Retrieve Latest Invoice\n→ Verify invoice status = 'paid'\n→ Subscription auto-pays via attached PM"] --> H
    H["🔄 Update Subscription\nSubscriptionTests — update metadata"] --> I
    I["❌ Cancel Subscription\nSubscriptionTests.TC_Cancel\n→ status = 'canceled'"] --> J
    J([✅ Complete])

    style A fill:#6B21A8,color:#fff
    style J fill:#166534,color:#fff
```

---

## 3. Saved Card E2E Flow

**Suite:** `testng-saved-card-e2e.xml` | **Group:** `saved_card_e2e`

This flow demonstrates using a SetupIntent to save a card for off-session (recurring) payments.

```mermaid
flowchart TD
    A([🚀 Start]) --> B
    B["👤 Create Customer"] --> C
    C["💳 Create Payment Method\n(tok_bypassPending)"] --> D
    D["📎 Attach PM to Customer"] --> E
    E["🔧 Create SetupIntent\n→ purpose = off_session\n→ Saves the PM for future charges"] --> F
    F["✅ Confirm SetupIntent\n→ status = 'succeeded'\n→ PM is now saved"] --> G
    G["💰 Create Payment Intent\n→ confirm = true\n→ off_session = true\n→ Uses the saved payment_method"] --> H
    H["✅ Payment Succeeds\n→ Funds available instantly\n→ TestContext.setPaymentIntentId()"] --> I
    I([✅ Complete])

    style A fill:#6B21A8,color:#fff
    style I fill:#166534,color:#fff
```

---

## 4. Invoice Lifecycle Flow

**Suite:** `testng-invoices.xml` | **Group:** `invoice`

```mermaid
flowchart LR
    A([Draft]) -->|finalize| B([Open])
    B -->|pay| C([Paid ✅])
    B -->|void| D([Void 🚫])
    B -->|mark_uncollectible| E([Uncollectible ⚠️])
    A -->|delete| F([Deleted 🗑️])

    style A fill:#92400E,color:#fff
    style B fill:#1D4ED8,color:#fff
    style C fill:#166534,color:#fff
    style D fill:#374151,color:#fff
    style E fill:#7C3AED,color:#fff
    style F fill:#991B1B,color:#fff
```

**Test flow:**

```mermaid
flowchart TD
    A([Start]) --> B
    B["👤 Create/Reuse Customer\n(TestContext or fallback)"] --> C
    C["🧾 Add Invoice Item\n/v1/invoiceitems\n(required for non-zero invoice)"] --> D
    D["📄 TC_01: Create Draft Invoice\nstatus = 'draft'"] --> E
    E["✅ TC_02: Finalize Invoice\nstatus = 'open'"] --> F
    F["💳 TC_03: Pay Invoice\nstatus = 'paid'\namount_remaining = 0"] --> G
    G([Complete])

    style A fill:#6B21A8,color:#fff
    style G fill:#166534,color:#fff
```

---

## 5. Dispute E2E Flow

**Suite:** `testng-disputes-e2e.xml` | **Group:** `disputes_e2e`

```mermaid
flowchart TD
    A([Start]) --> B
    B["💳 Create Payment with\ndisputable test card\n(tok_createDispute)"] --> C
    C["🚨 Dispute Created\nAutomatically by Stripe\nstatus = 'needs_response'"] --> D
    D["📝 Submit Evidence\nDisputesTest — update with evidence text\nstatus = 'evidence_under_review'"] --> E
    E["🔒 Close Dispute\nDisputesTest — mark as lost/won"] --> F
    F([Complete])

    style A fill:#6B21A8,color:#fff
    style F fill:#166534,color:#fff
    style C fill:#991B1B,color:#fff
```

---

## 6. Transfer with `source_transaction` — Balance Safety

This pattern is used in `TransferTests.TC_01` and `TransfersHelper.createFallbackTransfer()` to avoid `balance_insufficient` errors in Stripe test mode.

```mermaid
flowchart TD
    A([Start Transfer Flow]) --> B
    B{"Is there a confirmed\nPaymentIntent in context?"}
    B -->|Yes| C["Read TestContext.getPaymentIntentId()\nRetrieve PI → get latest_charge"]
    B -->|No| D["PaymentIntentHelper.createFallbackPaymentIntent(true)\nCreate + confirm fresh PI\n→ tok_bypassPending ensures instant available balance"]
    C --> E
    D --> E
    E["Read latest_charge ID from PI\nThis is the Charge object on the PI"]
    E --> F["POST /v1/transfers\nbody:\n  amount = amount_received\n  destination = merchant_account_id\n  source_transaction = chargeId  ← KEY FIELD"]
    F --> G{Transfer succeeds?}
    G -->|Yes ✅| H["Funds moved to connected account\nwithout touching platform balance"]
    G -->|No ❌ balance_insufficient| I["❌ This should NOT happen if source_transaction was set correctly"]

    style A fill:#6B21A8,color:#fff
    style H fill:#166534,color:#fff
    style I fill:#991B1B,color:#fff
```

---

## 7. Standalone vs. Suite Test Execution

Every test class is designed to run both independently and as part of a suite, using the fallback pattern.

```mermaid
flowchart TD
    A(["Test method starts"]) --> B

    B{"TestContext.getPaymentIntentId()\n= null?"}

    B -->|"Not null (suite run)"| C["✅ Use existing PI from context\nPrevious test step already created it"]
    B -->|"null (standalone run)"| D["🔧 PaymentIntentHelper.createFallbackPaymentIntent(true)\nCreate everything fresh: Customer + PM + PI"]

    C --> E["Run the actual test assertions"]
    D --> E

    E --> F["TestContext.set*(createdId)\nStore for use by next test in suite"]
    F --> G([Done])

    style A fill:#6B21A8,color:#fff
    style G fill:#166534,color:#fff
    style C fill:#166534,color:#fff
    style D fill:#92400E,color:#fff
```

---

## 8. Balance Management in Test Mode

Stripe test mode uses `pending` balance by default. These patterns ensure tests never hit `balance_insufficient`.

```mermaid
flowchart TD
    A(["Payment needs to succeed"]) --> B

    B["Use tok_bypassPending card token\nin PaymentMethodsHelper.createValidPaymentMethod()"]
    B --> C["Charge settles INSTANTLY to\navailable balance (not pending)"]

    C --> D{"Which account needs the funds?"}
    D -->|"Platform account (for payouts/transfers)"| E["Platform balance is already funded\nby the confirmed PaymentIntent"]
    D -->|"Connected account (for payouts on merchant)"| F{"Is this a suite run?"}

    F -->|Yes| G["Transfer already ran in previous step\nConnected account has balance"]
    F -->|No standalone| H["TransfersHelper.createFallbackTransfer()\n→ Creates PI + Transfer with source_transaction\n→ Connected account receives funds"]

    E --> I([Payout / Transfer Succeeds ✅])
    G --> I
    H --> I

    style A fill:#6B21A8,color:#fff
    style I fill:#166534,color:#fff
    style B fill:#1D4ED8,color:#fff
```

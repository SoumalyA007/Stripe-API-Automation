# 🔷 Stripe API Automation Framework

<div align="center">

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![TestNG](https://img.shields.io/badge/TestNG-7.9.0-FF6C37?style=for-the-badge)
![RestAssured](https://img.shields.io/badge/RestAssured-6.0.0-4CAF50?style=for-the-badge)
![Jenkins](https://img.shields.io/badge/Jenkins-CI%2FCD-D24939?style=for-the-badge&logo=jenkins&logoColor=white)
![Stripe](https://img.shields.io/badge/Stripe-API-635BFF?style=for-the-badge&logo=stripe&logoColor=white)

**A production-grade REST API test automation framework for the Stripe payment platform**

[📖 Architecture](docs/ARCHITECTURE.md) · [🧪 Test Suites](docs/TEST_SUITES.md) · [🔄 E2E Flows](docs/E2E_FLOWS.md) · [⚙️ Configuration](docs/CONFIGURATION.md) · [🚀 CI/CD](docs/CICD.md)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [What's Tested](#-whats-tested)
- [Quick Start](#-quick-start)
- [Project Structure](#-project-structure)
- [Running Tests](#-running-tests)
- [Reports & Logs](#-reports--logs)
- [Documentation Index](#-documentation-index)

---

## 🎯 Overview

This framework provides **comprehensive automated API testing** for the Stripe payment platform using a clean, layered architecture. It covers the complete payment lifecycle — from creating customers and payment methods through payments, subscriptions, invoices, transfers, payouts, refunds, disputes, and webhook event verification.

### Key Capabilities

| Feature | Details |
|---|---|
| **API Coverage** | 14 Stripe resources, 150+ test cases |
| **Test Types** | Positive, Negative, Auth, E2E, Idempotency |
| **E2E Flows** | Marketplace, Subscription, Saved Card, Dispute |
| **CI/CD** | Jenkins-ready with secrets via Credentials Store |
| **Reporting** | ExtentReports (HTML) + Log4j2 rolling logs |
| **POJO Validation** | Jakarta Bean Validation on every API response |
| **Data-Driven** | TestNG DataProviders for parameterised tests |

---

## 📦 What's Tested

```
Stripe Resources Covered
│
├── 👤 Customers           – Create, Read, Update, Delete, List
├── 💳 Payment Methods     – Create, Attach, Detach, Retrieve
├── 💰 Payment Intents     – Create, Confirm, Cancel, Retrieve
├── 🔧 Setup Intents       – Create, Confirm, Cancel, Retrieve
├── 📄 Subscriptions       – Create, Update, Cancel, Lifecycle
├── 🧾 Invoices            – Draft → Finalize → Pay / Void / Uncollectible
├── 💸 Transfers           – Create, Retrieve, Reverse (with source_transaction)
├── 🏧 Payouts             – Create, Retrieve, Cancel
├── 🔙 Refunds             – Full, Partial, Cancel
├── ⚖️  Disputes            – Create, Update, Close, Evidence submission
├── 🔔 Webhook Events      – customer.created, charge.refunded
├── 🛡️  Radar               – Early Fraud Warnings
├── 🏦 Connected Accounts  – Create, Retrieve, Update
└── 🔑 Accounts (Platform) – Retrieve, Update
```

---

## ⚡ Quick Start

### Prerequisites

| Tool | Version |
|---|---|
| Java JDK | 21+ |
| Apache Maven | 3.6+ |
| Stripe Account | Test mode access |

### 1. Clone the Repository

```bash
git clone https://github.com/<your-username>/Stripe-API-Automation.git
cd Stripe-API-Automation
```

### 2. Configure Credentials

Create `src/test/resources/config.properties` (**this file is gitignored — never commit it**):

```properties
baseURI=https://api.stripe.com
authKey=sk_test_YOUR_SECRET_KEY_HERE
merchant_account_id=acct_YOUR_CONNECTED_ACCOUNT_ID
amount=2000
```

> **Note:** `amount` is in cents. `2000` = $20.00 USD.

### 3. Run the Full Suite

```bash
mvn clean test
```

### 4. Run a Specific Suite

```bash
# Marketplace E2E flow
mvn clean test -Dsurefire.suiteXmlFiles=testng-marketplace-e2e.xml

# Smoke tests only
mvn clean test -Dsurefire.suiteXmlFiles=testng-smoke.xml

# Regression suite
mvn clean test -Dsurefire.suiteXmlFiles=testng-regression.xml
```

---

## 📁 Project Structure

```
Stripe-API-Automation/
│
├── src/test/java/
│   ├── testbase/           # BaseClass – config loading, suite hooks, logging
│   ├── specification/      # RequestSpec & ResponseSpec builders
│   ├── endpoints/          # One class per Stripe resource (API call wrappers)
│   ├── helpers/            # Fallback creators, TestContext, PojoValidator
│   ├── dataprovider/       # TestNG @DataProvider classes (negative/edge cases)
│   ├── models/
│   │   ├── response/       # POJO classes for deserialising API responses
│   │   └── common/         # Shared model components
│   ├── listeners/          # ExtentReportListener, SuiteCleanupListener
│   ├── enums/              # Shared constants / enumerations
│   └── tests/              # Test classes (14 total)
│
├── src/test/resources/
│   ├── config.properties   # ← gitignored: local credentials
│   └── log4j2.xml          # Rolling file log configuration
│
├── testng*.xml             # 13 TestNG suite XML files
├── Jenkinsfile             # Declarative CI/CD pipeline
├── pom.xml                 # Maven build + dependencies
├── reports/                # ExtentReports HTML output
│   └── archive/            # Zipped older reports
└── docs/                   # Extended documentation
```

---

## ▶️ Running Tests

### Available Test Suites

| Suite File | Description |
|---|---|
| `testng.xml` | Full suite (flow + all regression) |
| `testng-regression.xml` | All regression tests |
| `testng-smoke.xml` | Fast sanity check (key paths only) |
| `testng-e2e.xml` | Combined E2E flows |
| `testng-marketplace-e2e.xml` | Marketplace payment flow (11 ordered steps) |
| `testng-subscription-e2e.xml` | Subscription billing flow |
| `testng-saved-card-e2e.xml` | SetupIntent → saved card → future payment |
| `testng-disputes-e2e.xml` | Dispute lifecycle flow |
| `testng-invoices.xml` | Invoice lifecycle tests |
| `testng-idempotency.xml` | Idempotency key tests |
| `testng-negative.xml` | All negative/invalid-input tests |
| `testng-auth.xml` | Authentication failure tests |
| `testng-flow.xml` | Core user flow tests |

### Run by Test Group

```bash
# Run only idempotency tests
mvn clean test -Dgroups=idempotent_test

# Run all negative tests
mvn clean test -Dgroups=negative

# Run invoice tests
mvn clean test -Dgroups=invoice
```

---

## 📊 Reports & Logs

### ExtentReports (HTML)

Generated in `reports/` after every run:
```
reports/
├── ExtentReport_2026-07-04_12-30-00.html   ← current run
└── archive/
    └── ExtentReport_2026-07-03_18-00-00.zip ← archived previous runs
```

Open any `.html` file in a browser for a full visual test report with pass/fail status, request/response details, and timestamps.

### Log4j2 (Rolling File Logs)

Generated in `target/logs/` after every run:
```
target/logs/
├── test_execution.log                          ← live log (current run)
└── archive/
    └── test_execution_2026-07-03_18-00-00.log ← previous runs
```

---

## 📚 Documentation Index

| Document | Description |
|---|---|
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | Framework layers, design patterns, class relationships |
| [TEST_SUITES.md](docs/TEST_SUITES.md) | All test suites, groups, and test case inventory |
| [E2E_FLOWS.md](docs/E2E_FLOWS.md) | End-to-end flow diagrams for all scenarios |
| [CONFIGURATION.md](docs/CONFIGURATION.md) | Config properties, environment variables, Stripe test tokens |
| [CICD.md](docs/CICD.md) | Jenkins setup guide — Freestyle and Pipeline |

---

## 🛠️ Technology Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Language |
| Maven | 3.x | Build tool & dependency management |
| TestNG | 7.9.0 | Test runner & assertions |
| REST Assured | 6.0.0 | HTTP client & API assertions |
| Hamcrest | 3.0 | Matcher library |
| Jackson Databind | 2.18.3 | JSON deserialization to POJOs |
| Hibernate Validator | 8.0.2 | POJO field validation (Jakarta Bean Validation) |
| ExtentReports | 5.1.2 | HTML test reports |
| Log4j2 | 2.25.2 | Structured logging |
| Lombok | 1.18.42 | Boilerplate reduction (`@Data`, `@NoArgsConstructor`) |
| JavaFaker | 1.0.2 | Test data generation |
| Apache POI | 5.5.1 | Excel data support |

---

<div align="center">
Built with ❤️ for the Stripe API · Java 21 · TestNG · REST Assured
</div>
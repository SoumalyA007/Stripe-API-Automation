package dataprovider;

import org.testng.annotations.DataProvider;
import specification.ResponseSpec;

import java.util.HashMap;
import java.util.Map;

public class PaymentMethodsDataProvider {

        @DataProvider(name = "createPaymentMethod")
        public Object[][] createPaymentMethod() {

                Map<String, Object> validCard = new HashMap<>();
                validCard.put("type", "card");
                validCard.put("card[token]", "tok_visa");

                return new Object[][] {
                                { "Card Payment Method", validCard }
                };

        }

        // Use this in Payment Intent method
        // @DataProvider(name = "createInvalidPaymentMethod")
        // public Object[][] createInvalidPaymentMethod(){
        //
        // Map<String, Object> declinedCard = new HashMap<>();
        // declinedCard.put("type", "card");
        // declinedCard.put("card[token]", "tok_visa_chargeDeclinedInsufficientFunds");
        //
        // Map<String, Object> insufficientBalanceCard = new HashMap<>();
        // insufficientBalanceCard.put("type", "card");
        // insufficientBalanceCard.put("card[token]",
        // "tok_visa_chargeDeclinedInsufficientFunds");
        //
        // Map<String, Object> lostCard = new HashMap<>();
        // lostCard.put("type", "card");
        // lostCard.put("card[token]", "tok_visa_chargeDeclinedLostCard");
        //
        // Map<String, Object> expiredCard = new HashMap<>();
        // expiredCard.put("type", "card");
        // expiredCard.put("card[token]", "tok_visa_chargeDeclinedExpiredCard");
        //
        // Map<String, Object> incorrectCvc = new HashMap<>();
        // incorrectCvc.put("type", "card");
        // incorrectCvc.put("card[token]", "tok_visa_chargeDeclinedIncorrectCvc");
        //
        //
        //
        //
        //
        // return new Object[][]{
        // { "Card Payment Method", declinedCard, "card_declined" },
        // { "Card Payment Method", insufficientBalanceCard, "card_declined" },
        // { "Card Payment Method", lostCard, "card_declined" },
        // { "Card Payment Method", expiredCard, "expired_card" },
        // { "Card Payment Method", incorrectCvc, "incorrect_cvc" },
        //
        // };
        //
        // }

        @DataProvider(name = "createInvalidPaymentMethod")
        public Object[][] createInvalidPaymentMethod() {

                Map<String, Object> missingType = new HashMap<>();
                missingType.put("card[token]", "tok_visa");

                Map<String, Object> invalidToken = new HashMap<>();
                invalidToken.put("type", "card");
                invalidToken.put("card[token]", "tok_invalid");

                Map<String, Object> emptyRequest = new HashMap<>();

                Map<String, Object> invalidType = new HashMap<>();
                invalidType.put("type", "bitcoin");

                Map<String, Object> invalidParam = new HashMap<>();
                invalidParam.put("type", "card");
                invalidParam.put("card[abc]", "xyz");

                Map<String, Object> randomToken = new HashMap<>();
                randomToken.put("type", "card");
                randomToken.put("card[token]", "12345");

                return new Object[][] {
                                { "Missing Card Type", missingType },
                                { "Invalid Card Token", invalidToken },
                                { "Empty Card Request", emptyRequest },
                                { "Invalid Card Type", invalidType },
                                { "Invalid Card Parameter", invalidParam },
                                { "Random Card Token", randomToken }
                };
        }

        @DataProvider(name = "attachPaymentMethodNegative")
        public Object[][] attachPaymentMethodNegative() {

                return new Object[][] {

                                // invalid customer id + valid payment method
                                {
                                                "cus_invalid123",
                                                null,
                                                ResponseSpec.bad_request(),
                                                "No such customer"
                                },

                                // valid customer id + invalid payment method
                                {
                                                null, // use valid customer from TestContext
                                                "pm_invalid123",
                                                ResponseSpec.not_found(),
                                                "No such PaymentMethod"
                                },

                                // both invalid
                                {
                                                null,
                                                "pm_invalid123",
                                                ResponseSpec.not_found(),
                                                "No such"
                                },

                                // null customer
                                {
                                                "",
                                                "pm_valid123",
                                                ResponseSpec.bad_request(),
                                                "customer"
                                }
                };
        }

        @DataProvider(name = "retrieveInvalidPaymentMethodByCustomer")
        public Object[][] retrieveInvalidPaymentMethodByCustomer() {

                return new Object[][] {
                                // both invalid id
                                { "cust123", "pay123", ResponseSpec.not_found() },
                                // invalid customer Id
                                { "cust123", null, ResponseSpec.not_found() },
                                // invalid payment id
                                { null, "cust123", ResponseSpec.not_found() },
                                // blank ids
                                { "", "", ResponseSpec.not_found() },

                };
        }

        @DataProvider(name = "detachPaymentMethodNegativeCases")
        public Object[][] detachPaymentMethodNegativeCases() {
                return new Object[][] {
                                // invalid payment method id format
                                { "***", ResponseSpec.not_found(), "No such PaymentMethod" },
                                // non-existent payment method id
                                { "pm_1234567890abcdef", ResponseSpec.not_found(), "No such PaymentMethod" },
                                // already detached payment method (requires setup workflow)
                                { "SETUP_ALREADY_DETACHED", ResponseSpec.bad_request(), "not attached to a customer" }
                };
        }
}
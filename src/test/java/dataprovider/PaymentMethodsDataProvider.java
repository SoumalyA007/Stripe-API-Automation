package dataprovider;

import org.testng.annotations.DataProvider;

import java.util.HashMap;
import java.util.Map;

public class PaymentMethodsDataProvider {


    @DataProvider(name = "createPaymentMethod")
    public Object[][] createPaymentMethod() {

        Map<String, Object> validCard = new HashMap<>();
        validCard.put("type", "card");
        validCard.put("card[token]", "tok_visa");


        return new Object[][]{
                {"Card Payment Method", validCard}
        };

    }


    //Use this in Payment Intent method
//    @DataProvider(name = "createInvalidPaymentMethod")
//    public Object[][] createInvalidPaymentMethod(){
//
//        Map<String, Object> declinedCard = new HashMap<>();
//        declinedCard.put("type", "card");
//        declinedCard.put("card[token]", "tok_visa_chargeDeclinedInsufficientFunds");
//
//        Map<String, Object> insufficientBalanceCard = new HashMap<>();
//        insufficientBalanceCard.put("type", "card");
//        insufficientBalanceCard.put("card[token]", "tok_visa_chargeDeclinedInsufficientFunds");
//
//        Map<String, Object> lostCard = new HashMap<>();
//        lostCard.put("type", "card");
//        lostCard.put("card[token]", "tok_visa_chargeDeclinedLostCard");
//
//        Map<String, Object> expiredCard = new HashMap<>();
//        expiredCard.put("type", "card");
//        expiredCard.put("card[token]", "tok_visa_chargeDeclinedExpiredCard");
//
//        Map<String, Object> incorrectCvc = new HashMap<>();
//        incorrectCvc.put("type", "card");
//        incorrectCvc.put("card[token]", "tok_visa_chargeDeclinedIncorrectCvc");
//
//
//
//
//
//        return new Object[][]{
//                { "Card Payment Method", declinedCard, "card_declined" },
//                { "Card Payment Method", insufficientBalanceCard, "card_declined" },
//                { "Card Payment Method", lostCard, "card_declined" },
//                { "Card Payment Method", expiredCard, "expired_card" },
//                { "Card Payment Method", incorrectCvc, "incorrect_cvc" },
//
//        };
//
//    }

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

        return new Object[][]{
                {"Missing Card Type", missingType},
                {"Invalid Card Token", invalidToken},
                {"Empty Card Request", emptyRequest},
                {"Invalid Card Type", invalidType},
                {"Invalid Card Parameter", invalidParam},
                {"Random Card Token", randomToken}
        };
    }

    @DataProvider(name = "attachInvalidPaymentMethod")
    public Object[][] attachInvalidPaymentMethod(){




    }


}

package dataprovider;

import org.testng.annotations.DataProvider;

import java.util.HashMap;
import java.util.Map;

public class PaymentIntentDataProvider {

    // Use this in Payment Intent method
    @DataProvider(name = "createInvalidPaymentMethod")
    public Object[][] createInvalidPaymentMethod() {

        Map<String, Object> declinedCard = new HashMap<>();
        declinedCard.put("type", "card");
        declinedCard.put("card[token]", "tok_visa_chargeDeclinedInsufficientFunds");

        Map<String, Object> insufficientBalanceCard = new HashMap<>();
        insufficientBalanceCard.put("type", "card");
        insufficientBalanceCard.put("card[token]",
                "tok_visa_chargeDeclinedInsufficientFunds");

        Map<String, Object> lostCard = new HashMap<>();
        lostCard.put("type", "card");
        lostCard.put("card[token]", "tok_visa_chargeDeclinedLostCard");

        Map<String, Object> expiredCard = new HashMap<>();
        expiredCard.put("type", "card");
        expiredCard.put("card[token]", "tok_visa_chargeDeclinedExpiredCard");

        Map<String, Object> incorrectCvc = new HashMap<>();
        incorrectCvc.put("type", "card");
        incorrectCvc.put("card[token]", "tok_visa_chargeDeclinedIncorrectCvc");

        return new Object[][] {
                { "Card Payment Method", declinedCard, "card_declined" },
                { "Card Payment Method", insufficientBalanceCard, "card_declined" },
                { "Card Payment Method", lostCard, "card_declined" },
                { "Card Payment Method", expiredCard, "expired_card" },
                { "Card Payment Method", incorrectCvc, "incorrect_cvc" },

        };

    }

}
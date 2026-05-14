package dataprovider;

import org.testng.annotations.DataProvider;

import java.util.HashMap;
import java.util.Map;

public class PaymentMethodsDataProvider {


    @DataProvider(name = "createPaymentMethod")
    public Object[][] createPaymentMethod(){

        Map<String, Object> card = new HashMap<>();
        card.put("type", "card");
        card.put("card[number]", "4242424242424242");
        card.put("card[exp_month]", "12");
        card.put("card[exp_year]", "2028");
        card.put("card[cvc]", "123");

        Map<String, Object> bank = new HashMap<>();
        bank.put("type", "us_bank_account");
        bank.put("us_bank_account[account_holder_type]", "individual");
        bank.put("us_bank_account[account_number]", "000123456789");
        bank.put("us_bank_account[routing_number]", "110000000");

        return new Object[][]{
                { "Card Payment Method", card },
                { "US Bank Account", bank }
        };

    }

    @DataProvider(name = "createInvalidPaymentMethod")
    public Object[][] createInvalidPaymentMethod(){

        Map<String, Object> invalidCard = new HashMap<>();
        invalidCard.put("type", "card");
        invalidCard.put("card[number]", "400001");
        invalidCard.put("card[exp_month]", "12");
        invalidCard.put("card[exp_year]", "2028");
        invalidCard.put("card[cvc]", "123");


        Map<String, Object> invalidCardExpiryYear = new HashMap<>();
        invalidCardExpiryYear.put("type", "card");
        invalidCardExpiryYear.put("card[number]", "4242424242424242");
        invalidCardExpiryYear.put("card[exp_month]", "12");
        invalidCardExpiryYear.put("card[exp_year]", "2020");
        invalidCardExpiryYear.put("card[cvc]", "123");

        Map<String, Object> invalidCardCVC = new HashMap<>();
        invalidCardCVC.put("type", "card");
        invalidCardCVC.put("card[number]", "4242424242424242");
        invalidCardCVC.put("card[exp_month]", "12");
        invalidCardCVC.put("card[exp_year]", "2028");
        invalidCardCVC.put("card[cvc]", "12");

        Map<String, Object> noCardNumber = new HashMap<>();
        noCardNumber.put("type", "card");
        noCardNumber.put("card[number]", null);
        noCardNumber.put("card[exp_month]", "12");
        noCardNumber.put("card[exp_year]", "2028");
        noCardNumber.put("card[cvc]", "123");

        Map<String, Object> unsupportedType = new HashMap<>();
        unsupportedType.put("type", "coinzz");
        unsupportedType.put("card[number]", "4242424242424242");
        unsupportedType.put("card[exp_month]", "12");
        unsupportedType.put("card[exp_year]", "2028");
        unsupportedType.put("card[cvc]", "123");

        return new Object[][]{
                { "Card Payment Method", invalidCard },
                { "Card Payment Method", invalidCardExpiryYear },
                { "Card Payment Method", invalidCardCVC },
                { "Card Payment Method", noCardNumber },
                { "Card Payment Method", unsupportedType },

        };

    }



}

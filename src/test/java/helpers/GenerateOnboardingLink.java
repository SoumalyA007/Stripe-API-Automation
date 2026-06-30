package helpers;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

public class GenerateOnboardingLink {

    // Replace with your Stripe Secret Key
    private static final String SECRET_KEY = "";

    public static void main(String[] args) {

        // ============================
        // STEP 1 : Create Express Account
        // ============================

        Map<String, Object> accountBody = new HashMap<>();
        accountBody.put("type", "express");
        accountBody.put("country", "US");
        accountBody.put("email", "merchantaccount@gmail.com"); // Your own email

        Response createAccount = RestAssured
                .given()
                .baseUri("https://api.stripe.com")
                .auth()
                .basic(SECRET_KEY, "")
                .contentType("application/x-www-form-urlencoded")
                .formParams(accountBody)
                .log().all()
                .post("/v1/accounts");

        createAccount.then().log().all();

        String accountId = createAccount.jsonPath().getString("id");

        System.out.println("\n=======================================");
        System.out.println("Connected Account : " + accountId);
        System.out.println("=======================================\n");


        // ============================
        // STEP 2 : Generate Onboarding Link
        // ============================

        Map<String, Object> onboardingBody = new HashMap<>();

        onboardingBody.put("account", accountId);
        onboardingBody.put("type", "account_onboarding");
        onboardingBody.put("refresh_url", "https://example.com/refresh");
        onboardingBody.put("return_url", "https://example.com/return");

        Response onboarding = RestAssured
                .given()
                .baseUri("https://api.stripe.com")
                .auth()
                .basic(SECRET_KEY, "")
                .contentType("application/x-www-form-urlencoded")
                .formParams(onboardingBody)
                .log().all()
                .post("/v1/account_links");

        onboarding.then().log().all();

        String onboardingUrl = onboarding.jsonPath().getString("url");

        System.out.println("\n=======================================");
        System.out.println("OPEN THIS URL IN BROWSER");
        System.out.println(onboardingUrl);
        System.out.println("=======================================\n");
    }
}
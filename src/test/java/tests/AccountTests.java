package tests;

import builders.requestbuilder.CreateAccountRequestPayload;
import com.github.javafaker.Faker;
import endpoints.accounts;
import helpers.Accounts;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import specification.ResponseSpec;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class AccountTests extends ResponseSpec {

    Faker faker = new Faker();

    private static ThreadLocal<Response> createAcountResponseThread = new ThreadLocal<>();

    @Test
    public void createValidAccount(){


        Response resp = accounts.createAccount(Accounts.validAccountCreationHelper());
        createAcountResponseThread.set(resp);
        resp.then().spec(OK());

    }

    //Create Invalid Cart by Sending Invalid Country Code
    @Test
    public void InvalidCreateCart(){


        CreateAccountRequestPayload requestPayload = Accounts.validAccountCreationHelper();
        requestPayload.getIdentity().setCountry("KW");

        Response resp = accounts.createAccount(requestPayload);

        resp.then().spec(bad_request())
                .body("error.message",containsString("Error: The full service agreement is not supported for accounts in KW") );

    }

}

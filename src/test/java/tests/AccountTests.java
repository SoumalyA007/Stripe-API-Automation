package tests;

import datafactory.CreateAccountDataFactory;
import endpoints.accounts;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import specification.ResponseSpec;

public class AccountTests extends ResponseSpec {

    private static ThreadLocal<Response> responseThreadLocal = new ThreadLocal<>();

    @Test
    public void createValidAccount(){

        Response resp = accounts.createAccount(CreateAccountDataFactory.createAccountRequestPayload());
        responseThreadLocal.set(resp);
        resp.then().spec(OK());

    }

}

package helpers;

import com.github.javafaker.Faker;

import endpoints.ConnectAccounts;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

public class ConnectedAccountHelper {
    private static final Faker faker = new Faker();

    public static String createConnectAccount(boolean isFlow) {

        Map<String, Object> body = new HashMap<>();
        body.put("type", "application");
        body.put("country", "US");
        body.put("email", faker.internet().emailAddress());
        Response response = ConnectAccounts.createConnectAccount(body);
        String connectAccountId = response.then()
                .extract()
                .jsonPath()
                .get("id");
        if (isFlow == true) {
            TestContext.setConnectAccountId(connectAccountId);
        }
        return connectAccountId;
    }

}

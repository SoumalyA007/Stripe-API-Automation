package endpoints;

import io.restassured.response.Response;
import specification.RequestSpec;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static testbase.BaseClass.p;

public class Invoices {

    // ============== INVOICE ITEMS ==============

    public static Response createInvoiceItem(Map<String, Object> body) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/invoiceitems")
                .formParams(body)
                .when()
                .post();
    }

    // ============== CREATE ==============

    public static Response createInvoice(Map<String, Object> body) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/invoices")
                .formParams(body)
                .when()
                .post();
    }

    public static Response createInvoiceWithCustomAuth(String token, Map<String, Object> body) {
        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v1/invoices")
                .contentType("application/x-www-form-urlencoded");
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return request.formParams(body).when().post();
    }

    // ============== RETRIEVE ==============

    public static Response retrieveInvoice(String invoiceId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/invoices/{id}")
                .pathParam("id", invoiceId)
                .when()
                .get();
    }

    public static Response retrieveInvoiceWithCustomAuth(String token, String invoiceId) {
        var request = given()
                .baseUri(p.getProperty("baseURI"))
                .basePath("/v1/invoices/{id}")
                .pathParam("id", invoiceId)
                .contentType("application/x-www-form-urlencoded");
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return request.when().get();
    }

    // ============== UPDATE ==============

    public static Response updateInvoice(String invoiceId, Map<String, Object> body) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/invoices/{id}")
                .pathParam("id", invoiceId)
                .formParams(body)
                .when()
                .post();
    }

    // ============== DELETE (draft only) ==============

    public static Response deleteInvoice(String invoiceId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/invoices/{id}")
                .pathParam("id", invoiceId)
                .when()
                .delete();
    }

    // ============== FINALIZE ==============

    public static Response finalizeInvoice(String invoiceId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/invoices/{id}/finalize")
                .pathParam("id", invoiceId)
                .when()
                .post();
    }

    // ============== PAY ==============

    public static Response payInvoice(String invoiceId) {
        return payInvoice(invoiceId, Map.of());
    }

    public static Response payInvoice(String invoiceId, Map<String, Object> body) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/invoices/{id}/pay")
                .pathParam("id", invoiceId)
                .formParams(body)
                .when()
                .post();
    }

    // ============== VOID ==============

    public static Response voidInvoice(String invoiceId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/invoices/{id}/void")
                .pathParam("id", invoiceId)
                .when()
                .post();
    }

    // ============== MARK UNCOLLECTIBLE ==============

    public static Response markUncollectible(String invoiceId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/invoices/{id}/mark_uncollectible")
                .pathParam("id", invoiceId)
                .when()
                .post();
    }

    // ============== SEND ==============

    public static Response sendInvoice(String invoiceId) {
        return given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/invoices/{id}/send")
                .pathParam("id", invoiceId)
                .when()
                .post();
    }

    // ============== LIST ==============

    public static Response listInvoices(Map<String, Object> queryParams) {
        var request = given()
                .spec(RequestSpec.setupv1())
                .basePath("/v1/invoices");
        if (queryParams != null && !queryParams.isEmpty()) {
            request.queryParams(queryParams);
        }
        return request.when().get();
    }
}

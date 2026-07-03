package dataprovider;

import org.testng.annotations.DataProvider;

import java.util.HashMap;
import java.util.Map;

public class InvoicesDataProvider {

    /**
     * Provides invalid/missing-field invoice creation payloads for negative tests.
     */
    @DataProvider(name = "invalidInvoicePayloads")
    public Object[][] invalidInvoicePayloads() {
        // Missing required 'customer'
        Map<String, Object> missingCustomer = new HashMap<>();

        // Invalid customer id
        Map<String, Object> invalidCustomer = new HashMap<>();
        invalidCustomer.put("customer", "cus_invalid_abc123");

        // Invalid collection method value
        Map<String, Object> invalidCollectionMethod = new HashMap<>();
        invalidCollectionMethod.put("customer", "PLACEHOLDER_WILL_BE_REPLACED");
        invalidCollectionMethod.put("collection_method", "invalid_method");

        return new Object[][] {
                { "Missing customer", missingCustomer },
                { "Invalid customer ID", invalidCustomer },
        };
    }

    /**
     * Provides invalid invoice IDs for negative retrieve tests.
     */
    @DataProvider(name = "invalidInvoiceIds")
    public Object[][] invalidInvoiceIds() {
        return new Object[][] {
                { "Random string", "in_invalid_12345abc", "No such invoice" },
                { "Wrong prefix", "ch_not_an_invoice", "No such invoice" },
                { "Blank ID", "   ", "No such invoice" },
        };
    }
}

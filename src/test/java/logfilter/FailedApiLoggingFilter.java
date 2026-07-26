package logfilter;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

/**
 * A REST-assured {@link Filter} that captures every request and its response
 * into a per-thread {@link RequestResponseLoggingBuffer}.
 *
 * <p>
 * Nothing is written to the log file here. The TestNG listener
 * ({@code ExtentReportListener#onTestFailure}) flushes the buffer to Log4j
 * <em>only</em> when a test fails, keeping the log file clean on green runs.
 * </p>
 */
public class FailedApiLoggingFilter implements Filter {

    @Override
    public Response filter(FilterableRequestSpecification req,
            FilterableResponseSpecification res,
            FilterContext ctx) {

        Response response = ctx.next(req, res);

        // ── Build a formatted snapshot of this request/response pair ──────────
        StringBuilder sb = new StringBuilder();
        sb.append("\n========== API REQUEST ==========\n");
        sb.append("Method  : ").append(req.getMethod()).append("\n");
        sb.append("URI     : ").append(req.getURI()).append("\n");
        sb.append("Headers :\n").append(req.getHeaders().toString()).append("\n");

        Object body = req.getBody();
        if (body != null) {
            sb.append("Body    :\n").append(body).append("\n");
        }

        sb.append("========== API RESPONSE ==========\n");
        sb.append("Status  : ").append(response.statusCode()).append("\n");
        sb.append("Headers :\n").append(response.getHeaders().toString()).append("\n");
        sb.append("Body    :\n").append(response.asPrettyString()).append("\n");
        sb.append("==================================");

        // Push into the thread-local buffer – the listener decides when to flush
        RequestResponseLoggingBuffer.append(sb.toString());

        return response;
    }
}

package logfilter;


import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FailedApiLoggingFilter implements Filter {

    private static final Logger logger = LogManager.getLogger(FailedApiLoggingFilter.class);

    @Override
    public Response filter(FilterableRequestSpecification req,
                           FilterableResponseSpecification res,
                           FilterContext ctx) {

        Response response = ctx.next(req, res);

        if (response.statusCode() >= 400) {

            logger.error("========== FAILED API REQUEST ==========");
            logger.error("Method : {}", req.getMethod());
            logger.error("URI    : {}", req.getURI());
            logger.error("Headers:\n{}", req.getHeaders());

            if (req.getBody() != null) {
                logger.error("Request Body:\n{}", req.getBody());
            }

            logger.error("========== FAILED API RESPONSE ==========");
            logger.error("Status : {}", response.statusCode());
            logger.error("Headers:\n{}", response.getHeaders());
            logger.error("Response Body:\n{}", response.asPrettyString());
        }

        return response;
    }
}

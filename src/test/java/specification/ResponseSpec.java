package specification;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

public class ResponseSpec {

    public static ResponseSpecification OK(){

        return new ResponseSpecBuilder().expectStatusCode(200).build();

    }

    public static ResponseSpecification bad_request(){

        return new ResponseSpecBuilder().expectStatusCode(400).build();

    }

    public static ResponseSpecification Unauthorized(){

        return new ResponseSpecBuilder().expectStatusCode(401).build();

    }

    public static ResponseSpecification request_failed(){

        return new ResponseSpecBuilder().expectStatusCode(402).build();

    }

    public static ResponseSpecification forbidden(){

        return new ResponseSpecBuilder().expectStatusCode(403).build();

    }

    public static ResponseSpecification not_found(){

        return new ResponseSpecBuilder().expectStatusCode(404).build();

    }

    public static ResponseSpecification Conflict(){

        return new ResponseSpecBuilder().expectStatusCode(409).build();

    }

    public static ResponseSpecification external_dependency_failed(){

        return new ResponseSpecBuilder().expectStatusCode(424).build();

    }

    public static ResponseSpecification too_many_requests(){

        return new ResponseSpecBuilder().expectStatusCode(429).build();

    }

    public static ResponseSpecification server_error_500(){

        return new ResponseSpecBuilder().expectStatusCode(500).build();

    }

    public static ResponseSpecification server_error_502(){

        return new ResponseSpecBuilder().expectStatusCode(502).build();

    }

    public static ResponseSpecification server_error_503(){

        return new ResponseSpecBuilder().expectStatusCode(503).build();

    }

    public static ResponseSpecification server_error_504(){

        return new ResponseSpecBuilder().expectStatusCode(504).build();

    }


}

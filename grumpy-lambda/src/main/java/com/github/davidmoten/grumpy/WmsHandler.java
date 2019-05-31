package com.github.davidmoten.grumpy;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.github.davidmoten.aws.helper.StandardRequestBodyPassThrough;

public class WmsHandler {

    public String get(Map<String, Object> input, Context context) {
        
        LambdaLogger log = context.getLogger();

        log.log("starting");

        // expects full request body passthrough from api gateway integration
        // request
        StandardRequestBodyPassThrough request = StandardRequestBodyPassThrough.from(input);
        return "";
    }

}

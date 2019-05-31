package com.github.davidmoten.grumpy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.davidmoten.aws.helper.StandardRequestBodyPassThrough;

public class WmsHandler {

    public String get(Map<String, Object> input, Context context) throws IOException {

        LambdaLogger log = context.getLogger();

        log.log("starting");

        // expects full request body passthrough from api gateway integration
        // request
        StandardRequestBodyPassThrough request = StandardRequestBodyPassThrough.from(input);
        try (InputStream is = WmsHandler.class.getResourceAsStream("/cloud.png")) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            int n;
            byte[] data = new byte[1024];
            while ((n = is.read(data, 0, data.length)) != -1) {
                bytes.write(data, 0, n);
            }

            bytes.flush();
            String base64 = Base64.getEncoder().encodeToString(bytes.toByteArray());
            String response = "{\"statusCode\": 200, " //
                    + "\"headers\":{\"Content-Type\": \"image/png\"}, " //
                    + "\"body\": \"" + base64 + "\", " //
                    + " \"isBase64Encoded\": true}";
            log.log("response=\n"+ response);
            return response;
        }
    }

}

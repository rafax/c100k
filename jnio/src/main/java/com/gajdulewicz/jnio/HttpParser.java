package com.gajdulewicz.jnio;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpParser {

    public static class HttpRequest {
        public final Map<String, String> parameters;
        public final String path;

        public HttpRequest(String[] req) {
            Map<String, String> params = new HashMap<>();
            path = req[0].split(" ", 3)[1];
            int queryStart = path.indexOf("?");
            if (queryStart == -1) {
                // no params
                parameters = Collections.unmodifiableMap(new HashMap<>());
                return;
            }
            int queryEnd = path.indexOf("#", queryStart);
            if (queryEnd == -1) {
                // no location, query ends at string end
                queryEnd = path.length();
            }
            String query = path.substring(queryStart, queryEnd);

            for (String param : query.split("&")) {
                String[] keyValue = param.split("=");
                String val = keyValue.length > 1 ? keyValue[1] : null;
                params.put(keyValue[0], val);
            }
            parameters = Collections.unmodifiableMap(params);
        }
    }


}

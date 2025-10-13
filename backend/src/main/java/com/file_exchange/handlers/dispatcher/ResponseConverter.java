package com.file_exchange.handlers.dispatcher;

import com.file_exchange.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Converts invocation results to HttpResponse
 */
public class ResponseConverter {
    private final ObjectMapper objectMapper;

    public ResponseConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public HttpResponse convertToResponse(Object result) throws IOException {
        if (result == null) {
            return HttpResponse.ok("".getBytes(), "text/plain");
        } else if (result instanceof String str) {
            return HttpResponse.ok(str.getBytes(), "text/plain");
        } else if (result instanceof byte[] bytes) {
            return HttpResponse.ok(bytes, "application/octet-stream");
        } else {
            String json = objectMapper.writeValueAsString(result);
            return HttpResponse.ok(json.getBytes(), "application/json");
        }
    }
}

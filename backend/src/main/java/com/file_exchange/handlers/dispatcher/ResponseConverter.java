package com.file_exchange.handlers.dispatcher;

import com.file_exchange.dto.FileDto;
import com.file_exchange.handlers.utilsFiles.MimeTypeUtils;
import com.file_exchange.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.io.IOException;
import java.io.InputStream;

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
        }else if(result instanceof FileDto fd){
            try (InputStream is = fd.getInputStream()) {
                byte[] data = is.readAllBytes();

                Map<String, String> headers = new HashMap<>();
                String filename = (fd.getFileName() != null && !fd.getFileName().isBlank())
                        ? fd.getFileName() : "download";
                headers.put("Content-Disposition", "attachment; filename=\"" + filename + "\"");

                String contentType = (fd.getContentType() != null && !fd.getContentType().isBlank())
                        ? fd.getContentType() : MimeTypeUtils.detect(fd.getFileName());

                return HttpResponse.ok(data, contentType, headers);
            }
        }else if(result instanceof InputStream in){
            // fallback: if somewhere else they return a clean stream
            try (InputStream is = in) {
                byte[] data = is.readAllBytes();
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Disposition", "attachment; filename=\"download\"");
                return HttpResponse.ok(data, "application/octet-stream", headers);
            }
        } else {
            String json = objectMapper.writeValueAsString(result);
            return HttpResponse.ok(json.getBytes(), "application/json");
        }
    }
}

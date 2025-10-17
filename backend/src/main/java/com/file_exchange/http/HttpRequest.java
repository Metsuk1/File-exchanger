package com.file_exchange.http;

import lombok.Getter;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import com.file_exchange.handlers.utilsFiles.TempFileInputStream;
import lombok.ToString;

@Getter
@ToString
public class HttpRequest {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final String body;
    private final Map<String, Object> parts;//for multipart -  parts

    public HttpRequest(String method, String path, Map<String, String> headers, String body, Map<String, Object> parts) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
        this.parts = parts != null ? parts : new HashMap<>();
    }

    public InputStream getPartAsStream(String partName) {
        Object part = parts.get(partName);
        return part instanceof InputStream ? (InputStream) part : null;
    }

    public String getPartAsString(String partName) {
        Object part = parts.get(partName);
        return part instanceof String ? (String) part : null;
    }

    public Long getPartAsLong(String partName) {
        String value = getPartAsString(partName);
        return value != null ? Long.parseLong(value) : null;
    }

    public TempFileInputStream getPartAsTempFile(String name) {
        Object part = parts.get(name);
        if (part instanceof TempFileInputStream) {
            return (TempFileInputStream) part;
        }
        return null;
    }

}
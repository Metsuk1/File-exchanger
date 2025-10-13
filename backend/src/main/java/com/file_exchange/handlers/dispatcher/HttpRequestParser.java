package com.file_exchange.handlers.dispatcher;

import com.file_exchange.http.HttpRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *  Parses incoming requests.
 */
public class HttpRequestParser {
    public HttpRequest parse(BufferedReader in) throws IOException {
        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            return null; // Invalid request
        }
        String[] parts = requestLine.split(" ");
        if (parts.length < 2) {
            return null; // Malformed
        }
        String method = parts[0].toUpperCase();
        String path = parts[1];
        Map<String, String> headers = parseHeaders(in);
        String body = parseBody(in, method, headers);
        return new HttpRequest(method, path, headers, body);
    }

    private Map<String, String> parseHeaders(BufferedReader in) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = in.readLine()) != null && !line.trim().isEmpty()) {
            int idx = line.indexOf(":");
            if (idx > 0) {
                headers.put(line.substring(0, idx).trim().toLowerCase(), line.substring(idx + 1).trim());
            }
        }
        return headers;
    }

    private String parseBody(BufferedReader in, String method, Map<String, String> headers) throws IOException {
        if (!isBodyExpected(method) || !headers.containsKey("content-length")) {
            return "";
        }
        int length;
        try {
            length = Integer.parseInt(headers.get("content-length"));
        } catch (NumberFormatException e) {
            return ""; // Invalid length
        }
        char[] buf = new char[length];
        int read = in.read(buf, 0, length);
        return (read == length) ? new String(buf) : "";
    }

    private boolean isBodyExpected(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
    }
}

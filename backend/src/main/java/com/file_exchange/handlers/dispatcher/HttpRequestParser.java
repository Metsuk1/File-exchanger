package com.file_exchange.handlers.dispatcher;

import com.file_exchange.http.HttpRequest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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

        if(isMultipart(headers)) {
            Map<String, Object> partsMap = parseMultipart(in, headers);
            return new HttpRequest(method, path, headers, "", partsMap);
        }else{
            String body = parseBody(in, method, headers);
            return new HttpRequest(method, path, headers, body, new HashMap<>());
        }
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

    private boolean isMultipart(Map<String, String> headers) {
        return headers.containsKey("content-type") && headers.get("content-type").contains("multipart/form-data");
    }

    private Map<String, Object> parseMultipart(BufferedReader in,Map<String, String> headers) throws IOException {
        Map<String, Object> parts = new HashMap<>();
        String boundary = extractBoundary(headers.get("content-type"));
        if (boundary == null) {
            throw new IOException("No boundary found in Content-Type");
        }

        String line;
        String currentPartName = null;
        StringBuilder partHeaders = new StringBuilder();
        StringBuilder textContent = new StringBuilder();
        byte[] binaryContent = null;
        boolean isBinary = false;

        while ((line = in.readLine()) != null) {
            if (line.startsWith("--" + boundary)) {
                if (currentPartName != null) {
                    if (isBinary && binaryContent != null) {
                        parts.put(currentPartName, new ByteArrayInputStream(binaryContent));
                    } else if (!textContent.toString().isEmpty()) {
                        parts.put(currentPartName, textContent.toString().trim());
                    }
                }
                currentPartName = null;
                partHeaders.setLength(0);
                textContent.setLength(0);
                binaryContent = null;
                isBinary = false;
            } else if (line.startsWith("Content-Disposition:") && line.contains("name=")) {
                currentPartName = extractPartName(line);
                partHeaders.append(line).append("\n");
            } else if (line.startsWith("Content-Type:") && currentPartName != null) {
                partHeaders.append(line).append("\n");
                isBinary = line.contains("application/octet-stream") || line.contains("image/") || line.contains("video/");
            } else if (currentPartName != null && !line.isEmpty() && binaryContent == null) {
                if (isBinary) {
                    binaryContent = readBinaryContent(in, boundary);
                } else {
                    textContent.append(line).append("\n");
                }
            }
        }

        if (currentPartName != null) {
            if (isBinary && binaryContent != null) {
                parts.put(currentPartName, new ByteArrayInputStream(binaryContent));
            } else if (!textContent.toString().isEmpty()) {
                parts.put(currentPartName, textContent.toString().trim());
            }
        }

        return parts;

    }

    private byte[] readBinaryContent(BufferedReader in, String boundary) throws IOException {
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null && !line.startsWith("--" + boundary)) {
            content.append(line).append("\n");
        }

        return content.toString().getBytes();
    }

    private String extractBoundary(String contentType) {
        if (contentType != null && contentType.contains("boundary=")) {
            return contentType.split("boundary=")[1].trim();
        }

        return null;
    }

    private String extractPartName(String line) {
        String[] parts = line.split("name=\"");
        if (parts.length > 1) {
            return parts[1].split("\"")[0];
        }

        return null;
    }
}

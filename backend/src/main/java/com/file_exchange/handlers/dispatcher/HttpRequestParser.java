package com.file_exchange.handlers.dispatcher;

import com.file_exchange.handlers.utilsFiles.InputStreamRequestContext;
import com.file_exchange.handlers.utilsFiles.TempFileInputStream;
import com.file_exchange.http.HttpRequest;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.util.Streams;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 *  Parses incoming requests.
 *  Supports:
 *  Regular text requests (e.g. JSON, form-urlencoded)
 *  Multipart/form-data requests (used for file uploads)
 */
public class HttpRequestParser {
    // Maximum size of data to hold in memory (5MB)
    private static final int MAX_IN_MEMORY_SIZE = 5 * 1024 * 1024;
    // Maximum allowed file size (100MB)
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    // Temporary directory used to store uploaded files before processing
    private final Path tempDirectory;

    public HttpRequestParser() {
        try {
            // Create temp directory for large files
            this.tempDirectory = Files.createTempDirectory("file-exchange-temp");
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp directory", e);
        }
    }

    /**
     * Main entry point for parsing an incoming HTTP request.
     * Reads from the provided InputStream and returns a populated HttpRequest object.
     */
    public HttpRequest parse(InputStream inputStream) throws IOException {
        BufferedInputStream bufferedStream = new BufferedInputStream(inputStream, 8192);

        ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
        int b;
        int matchIndex = 0;
        byte[] endMarker = "\r\n\r\n".getBytes(StandardCharsets.UTF_8);

        // Read the HTTP headers until the end marker (\r\n\r\n) is reached
         while ((b = bufferedStream.read()) != -1) {
            headerBuffer.write(b);

            if (b == endMarker[matchIndex]) {
                matchIndex++;
                if (matchIndex == endMarker.length) {
                    break; // End of headers section
                }
            } else {
                matchIndex = 0;
            }
        }

        String headerSection = headerBuffer.toString(StandardCharsets.UTF_8);
        String[] lines = headerSection.split("\r\n");

        // Parse the request line (it is like "POST /api/v1/files/upload" and so on)
        if (lines.length == 0) return null;
        String requestLine = lines[0];
        String[] parts = requestLine.split(" ");
        if (parts.length < 2) return null;

        String method = parts[0].toUpperCase();
        String path = parts[1];

        //Parse all other header lines into a Map
        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            int idx = line.indexOf(":");
            if (idx > 0) {
                headers.put(line.substring(0, idx).trim().toLowerCase(),// header name
                        line.substring(idx + 1).trim());      // header value
            }
        }

        /* Determine body type
            Check if the request is multipart/form-data (file upload)
        */
        if (isMultipart(headers)) {
            return parseMultipartStreaming(bufferedStream, method, path, headers);
        } else {
            // Otherwise, parse it as a normal text body
            String body = parseTextBody(bufferedStream, method, headers);
            return new HttpRequest(method, path, headers, body, new HashMap<>());
        }
    }

    /**
     * Parses multipart/form-data requests using Apache Commons FileUpload's streaming API.
     * This approach avoids loading entire files into memory at once.
     */
    private HttpRequest  parseMultipartStreaming(InputStream inputStream,
                                                 String method, String path,
                                                 Map<String, String> headers) throws IOException {
        String boundary = extractBoundary(headers.get("content-type"));
        if (boundary == null) throw new IOException("Missing multipart boundary");

        Map<String, Object> parts = new HashMap<>();

        try {
            RequestContext requestContext = new InputStreamRequestContext(inputStream, headers);

            FileUpload upload = new FileUpload();
            upload.setFileSizeMax(MAX_FILE_SIZE);
            upload.setSizeMax(MAX_FILE_SIZE);

            FileItemIterator iterator = upload.getItemIterator(requestContext);


            while (iterator.hasNext()) {
                FileItemStream item = iterator.next();
                String fieldName = item.getFieldName();

                if (item.isFormField()) {
                    // Simple form field — read as text
                    String value = Streams.asString(item.openStream(), StandardCharsets.UTF_8.name());
                    parts.put(fieldName, value);
                } else {
                    // File upload — stream the file to a temporary location
                    String fileName = item.getName();

                    // Create a temp file to store the uploaded content
                    Path tempFile = Files.createTempFile(tempDirectory, "upload-", ".tmp");
                    long bytesWritten = 0;

                    // Copy file data from the upload stream to the temp file
                    try (InputStream fileStream = item.openStream();
                         FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {

                        // copy file in 1 thread - only 8KB buffer in memory
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = fileStream.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                            bytesWritten += bytesRead;
                        }
                    }
                    // Wrap the temp file in a TempFileInputStream for later cleanup
                    parts.put(fieldName, new TempFileInputStream(tempFile, fileName, bytesWritten));
                }
            }
        } catch (FileUploadException e) {
            throw new IOException("Multipart parsing failed", e);
        }

        // Return a constructed HttpRequest object with parsed form data
        return new HttpRequest(method, path, headers, "", parts);
    }

    /**
     * Reads plain text request bodies (non-multipart).
     */
    private String parseTextBody(InputStream inputStream, String method,
                                 Map<String, String> headers) throws IOException {
        if (!isBodyExpected(method) || !headers.containsKey("content-length")) return "";

        int length = Integer.parseInt(headers.get("content-length"));
        byte[] buffer = new byte[length];

        int totalRead = 0;
        while (totalRead < length) {
            int read = inputStream.read(buffer, totalRead, length - totalRead);
            if (read == -1) break;
            totalRead += read;
        }

        return new String(buffer, 0, totalRead, StandardCharsets.UTF_8);
    }
    /**
     * Determines whether the HTTP method typically includes a request body.
     */
    private boolean isBodyExpected(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
    }
    /**
     * Checks if the request's Content-Type indicates a multipart/form-data request.
     */
    private boolean isMultipart(Map<String, String> headers) {
        return headers.containsKey("content-type") && headers.get("content-type").contains("multipart/form-data");
    }
    /**
     * Extracts the multipart boundary string from the Content-Type header.
     */
    private String extractBoundary(String contentType) {
        if (contentType == null) return null;
        int idx = contentType.indexOf("boundary=");
        return (idx != -1) ? contentType.substring(idx + 9).trim() : null;
    }

}

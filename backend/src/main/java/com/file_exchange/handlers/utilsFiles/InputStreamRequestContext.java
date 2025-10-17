package com.file_exchange.handlers.utilsFiles;

import org.apache.commons.fileupload.RequestContext;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Adapter for integration with Apache Commons FileUpload
 */
public class InputStreamRequestContext implements RequestContext {
    private final InputStream inputStream;
    private final Map<String, String> headers;

    public InputStreamRequestContext(InputStream inputStream, Map<String, String> headers) {
        this.inputStream = inputStream;
        this.headers = headers;
    }

    @Override
    public String getCharacterEncoding() {
        return StandardCharsets.UTF_8.name();
    }

    @Override
    public String getContentType() {
        return headers.get("content-type");
    }

    @Override
    public int getContentLength() {
        String length = headers.get("content-length");
        return length != null ? Integer.parseInt(length) : -1;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }
}

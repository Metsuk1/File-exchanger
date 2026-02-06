package com.file_exchange.unit.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import com.file_exchange.handlers.utilsFiles.InputStreamRequestContext;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("InputStreamRequestContext tests")
class InputStreamRequestContextTest {

    @Test
    @DisplayName("Should return UTF-8 character encoding")
    void testGetCharacterEncoding() {
        InputStream is = new ByteArrayInputStream("test".getBytes());
        Map<String, String> headers = new HashMap<>();

        InputStreamRequestContext context = new InputStreamRequestContext(is, headers);

        assertThat(context.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
    }

    @Test
    @DisplayName("Should return content type from headers")
    void testGetContentType() {
        InputStream is = new ByteArrayInputStream("test".getBytes());
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "multipart/form-data; boundary=----WebKitFormBoundary");

        InputStreamRequestContext context = new InputStreamRequestContext(is, headers);

        assertThat(context.getContentType()).isEqualTo("multipart/form-data; boundary=----WebKitFormBoundary");
    }

    @Test
    @DisplayName("Should return null content type when not present")
    void testGetContentTypeNull() {
        InputStream is = new ByteArrayInputStream("test".getBytes());
        Map<String, String> headers = new HashMap<>();

        InputStreamRequestContext context = new InputStreamRequestContext(is, headers);

        assertThat(context.getContentType()).isNull();
    }

    @Test
    @DisplayName("Should return content length from headers")
    void testGetContentLength() {
        InputStream is = new ByteArrayInputStream("test".getBytes());
        Map<String, String> headers = new HashMap<>();
        headers.put("content-length", "1024");

        InputStreamRequestContext context = new InputStreamRequestContext(is, headers);

        assertThat(context.getContentLength()).isEqualTo(1024);
    }

    @Test
    @DisplayName("Should return -1 when content length not present")
    void testGetContentLengthMissing() {
        InputStream is = new ByteArrayInputStream("test".getBytes());
        Map<String, String> headers = new HashMap<>();

        InputStreamRequestContext context = new InputStreamRequestContext(is, headers);

        assertThat(context.getContentLength()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Should return the input stream")
    void testGetInputStream() {
        InputStream is = new ByteArrayInputStream("test content".getBytes());
        Map<String, String> headers = new HashMap<>();

        InputStreamRequestContext context = new InputStreamRequestContext(is, headers);

        assertThat(context.getInputStream()).isEqualTo(is);
    }
}

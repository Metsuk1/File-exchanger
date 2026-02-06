package com.file_exchange.unit.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import com.file_exchange.handlers.dispatcher.HttpRequestParser;
import com.file_exchange.http.HttpRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("HttpRequestParser tests")
class HttpRequestParserTest {

    private HttpRequestParser parser;

    @BeforeEach
    void setUp() {
        parser = new HttpRequestParser();
    }

    @Test
    @DisplayName("Should parse GET request without body")
    void testParseGetRequest() throws IOException {
        String request =
                "GET /api/v1/files HTTP/1.1\r\n" + "Host: localhost:8080\r\n" + "Accept: application/json\r\n" + "\r\n";

        InputStream inputStream = new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
        HttpRequest result = parser.parse(inputStream);

        assertThat(result).isNotNull();
        assertThat(result.getMethod()).isEqualTo("GET");
        assertThat(result.getPath()).isEqualTo("/api/v1/files");
    }

    @Test
    @DisplayName("Should parse POST request with JSON body")
    void testParsePostRequestWithBody() throws IOException {
        String body = "{\"email\":\"test@test.com\",\"password\":\"123456\"}";
        String request = "POST /api/v1/users/login HTTP/1.1\r\n" + "Host: localhost:8080\r\n"
                + "Content-Type: application/json\r\n"
                + "Content-Length: " + body.length() + "\r\n"
                + "\r\n"
                + body;

        InputStream inputStream = new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
        HttpRequest result = parser.parse(inputStream);

        assertThat(result).isNotNull();
        assertThat(result.getMethod()).isEqualTo("POST");
        assertThat(result.getPath()).isEqualTo("/api/v1/users/login");
        assertThat(result.getBody()).isEqualTo(body);
    }

    @Test
    @DisplayName("Should parse query parameters")
    void testParseQueryParams() throws IOException {
        String request = "GET /api/v1/files?fileId=123&userId=456 HTTP/1.1\r\n" + "Host: localhost:8080\r\n" + "\r\n";

        InputStream inputStream = new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
        HttpRequest result = parser.parse(inputStream);

        assertThat(result).isNotNull();
        assertThat(result.getPath()).isEqualTo("/api/v1/files");
        assertThat(result.getQueryParams()).containsEntry("fileId", "123");
        assertThat(result.getQueryParams()).containsEntry("userId", "456");
    }

    @Test
    @DisplayName("Should parse headers case-insensitively")
    void testParseHeadersCaseInsensitive() throws IOException {
        String request = "GET /api/v1/files HTTP/1.1\r\n" + "Host: localhost:8080\r\n"
                + "Authorization: Bearer token123\r\n" + "Content-Type: application/json\r\n" + "\r\n";

        InputStream inputStream = new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
        HttpRequest result = parser.parse(inputStream);

        assertThat(result).isNotNull();
        assertThat(result.getHeaders()).containsEntry("authorization", "Bearer token123");
        assertThat(result.getHeaders()).containsEntry("content-type", "application/json");
    }

    @Test
    @DisplayName("Should return null for empty request")
    void testParseEmptyRequest() throws IOException {
        String request = "\r\n\r\n";

        InputStream inputStream = new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
        HttpRequest result = parser.parse(inputStream);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null for malformed request line")
    void testParseMalformedRequestLine() throws IOException {
        String request = "INVALID\r\n\r\n";

        InputStream inputStream = new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
        HttpRequest result = parser.parse(inputStream);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle PUT request with body")
    void testParsePutRequest() throws IOException {
        String body = "{\"name\":\"updated\"}";
        String request = "PUT /api/v1/users/1 HTTP/1.1\r\n" + "Host: localhost:8080\r\n"
                + "Content-Type: application/json\r\n"
                + "Content-Length: " + body.length() + "\r\n"
                + "\r\n"
                + body;

        InputStream inputStream = new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
        HttpRequest result = parser.parse(inputStream);

        assertThat(result).isNotNull();
        assertThat(result.getMethod()).isEqualTo("PUT");
        assertThat(result.getBody()).isEqualTo(body);
    }

    @Test
    @DisplayName("Should handle PATCH request with body")
    void testParsePatchRequest() throws IOException {
        String body = "{\"name\":\"patched\"}";
        String request = "PATCH /api/v1/users/1 HTTP/1.1\r\n" + "Host: localhost:8080\r\n"
                + "Content-Type: application/json\r\n"
                + "Content-Length: " + body.length() + "\r\n"
                + "\r\n"
                + body;

        InputStream inputStream = new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
        HttpRequest result = parser.parse(inputStream);

        assertThat(result).isNotNull();
        assertThat(result.getMethod()).isEqualTo("PATCH");
        assertThat(result.getBody()).isEqualTo(body);
    }

    @Test
    @DisplayName("Should handle DELETE request")
    void testParseDeleteRequest() throws IOException {
        String request = "DELETE /api/v1/files?fileId=123 HTTP/1.1\r\n" + "Host: localhost:8080\r\n"
                + "Authorization: Bearer token\r\n" + "\r\n";

        InputStream inputStream = new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
        HttpRequest result = parser.parse(inputStream);

        assertThat(result).isNotNull();
        assertThat(result.getMethod()).isEqualTo("DELETE");
        assertThat(result.getQueryParams()).containsEntry("fileId", "123");
    }

    @Test
    @DisplayName("Should handle GET request without content-length")
    void testParseGetWithoutContentLength() throws IOException {
        String request = "GET /api/v1/files HTTP/1.1\r\n" + "Host: localhost:8080\r\n" + "\r\n";

        InputStream inputStream = new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
        HttpRequest result = parser.parse(inputStream);

        assertThat(result).isNotNull();
        assertThat(result.getBody()).isEmpty();
    }

    @Test
    @DisplayName("Should skip empty header lines")
    void testSkipEmptyHeaderLines() throws IOException {
        String request = "GET /api/v1/files HTTP/1.1\r\n" + "Host: localhost:8080\r\n" + "   \r\n"
                + "Accept: application/json\r\n" + "\r\n";

        InputStream inputStream = new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8));
        HttpRequest result = parser.parse(inputStream);

        assertThat(result).isNotNull();
        assertThat(result.getHeaders()).containsEntry("accept", "application/json");
    }
}

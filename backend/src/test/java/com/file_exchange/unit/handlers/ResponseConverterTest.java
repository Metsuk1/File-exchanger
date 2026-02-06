package com.file_exchange.unit.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.file_exchange.dto.FileDto;
import com.file_exchange.handlers.dispatcher.ResponseConverter;
import com.file_exchange.http.HttpResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ResponseConverter tests")
class ResponseConverterTest {

    private ResponseConverter responseConverter;

    @BeforeEach
    void setUp() {
        responseConverter = new ResponseConverter(new ObjectMapper());
    }

    @Test
    @DisplayName("Should convert null to empty response")
    void testNullResult() throws IOException {
        HttpResponse response = responseConverter.convertToResponse(null);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should convert String to text/plain response")
    void testStringResult() throws IOException {
        HttpResponse response = responseConverter.convertToResponse("Hello World");

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(new String(response.getBody())).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("Should convert byte array to octet-stream response")
    void testByteArrayResult() throws IOException {
        byte[] data = "binary data".getBytes();
        HttpResponse response = responseConverter.convertToResponse(data);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(data);
    }

    @Test
    @DisplayName("Should convert FileDto to attachment response")
    void testFileDtoResult() throws IOException {
        byte[] content = "file content".getBytes();
        InputStream is = new ByteArrayInputStream(content);
        FileDto fileDto = new FileDto("test.txt", "text/plain", is);

        HttpResponse response = responseConverter.convertToResponse(fileDto);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(content);
    }

    @Test
    @DisplayName("Should convert FileDto with null filename")
    void testFileDtoNullFilename() throws IOException {
        byte[] content = "file content".getBytes();
        InputStream is = new ByteArrayInputStream(content);
        FileDto fileDto = new FileDto(null, "application/octet-stream", is);

        HttpResponse response = responseConverter.convertToResponse(fileDto);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should convert FileDto with blank content type")
    void testFileDtoBlankContentType() throws IOException {
        byte[] content = "file content".getBytes();
        InputStream is = new ByteArrayInputStream(content);
        FileDto fileDto = new FileDto("test.png", "", is);

        HttpResponse response = responseConverter.convertToResponse(fileDto);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should convert InputStream to attachment response")
    void testInputStreamResult() throws IOException {
        byte[] content = "stream content".getBytes();
        InputStream is = new ByteArrayInputStream(content);

        HttpResponse response = responseConverter.convertToResponse(is);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(content);
    }

    @Test
    @DisplayName("Should convert object to JSON response")
    void testObjectResult() throws IOException {
        Map<String, String> data = Map.of("key", "value");

        HttpResponse response = responseConverter.convertToResponse(data);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(new String(response.getBody())).contains("\"key\"");
        assertThat(new String(response.getBody())).contains("\"value\"");
    }
}

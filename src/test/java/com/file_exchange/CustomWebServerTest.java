package com.file_exchange;

import com.file_exchange.controllers.UserController;
import com.file_exchange.dto.UserDto;
import com.file_exchange.repository.UserRepository;
import com.file_exchange.server.CustomWebServer;
import com.file_exchange.services.UserService;
import  com.file_exchange.HttpResponseData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CustomWebServerTest {
    private static CustomWebServer server;
    private static final int PORT = 8080;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private UserDto createdUser;

    @BeforeEach
    void startServer() throws IOException {
        server = new CustomWebServer(PORT, 50, true);
        server.registerController(new UserController(new UserService(new UserRepository())));
        server.start();
        waitForServerStart();
    }

    private void waitForServerStart() throws IOException {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 5000) {
            try (Socket socket = new Socket("localhost", PORT)) {
                return; // Server is up
            } catch (IOException e) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new IOException("Server did not start within 5 seconds");
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    // Helper method to send HTTP requests
    private HttpResponseData sendRequest(String method, String path, String jsonBody) throws IOException {
        URL url = new URL("http://localhost:" + PORT + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        if (jsonBody != null) {
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
        }

        int statusCode = conn.getResponseCode();
        String responseBody;

        if (statusCode >= 400) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                responseBody = reader.lines().collect(java.util.stream.Collectors.joining("\n"));
            }
        } else {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                responseBody = reader.lines().collect(java.util.stream.Collectors.joining("\n"));
            }
        }

        conn.disconnect();
        return new HttpResponseData(statusCode, responseBody);
    }

    @Test
    @Timeout(10)
    void testCreateUser() throws IOException {
        String jsonInput = "{\"name\":\"John Doe\",\"email\":\"john@example.com\"}";
        HttpResponseData response = sendRequest("POST", "/api/v1/users", jsonInput);

        assertEquals(200, response.statusCode, "Expected HTTP 200 OK for POST");

        createdUser = objectMapper.readValue(response.body, UserDto.class);
        assertEquals("John Doe", createdUser.getName(), "Name should match");
        assertEquals("john@example.com", createdUser.getEmail(), "Email should match");
        assertNotNull(createdUser.getId(), "ID should be assigned");
    }

    @Test
    @Timeout(10)
    void testGetAllUsers() throws IOException {
        // Create a user first
        String jsonInput = "{\"name\":\"John Doe\",\"email\":\"john@example.com\"}";
        HttpResponseData createResponse = sendRequest("POST", "/api/v1/users", jsonInput);
        assertEquals(200, createResponse.statusCode, "Expected HTTP 200 OK for POST setup");

        HttpResponseData response = sendRequest("GET", "/api/v1/users", null);

        assertEquals(200, response.statusCode, "Expected HTTP 200 OK for GET all");

        List<UserDto> users = objectMapper.readValue(response.body, objectMapper.getTypeFactory().constructCollectionType(List.class, UserDto.class));
        assertEquals(1, users.size(), "Should return one user");
        assertEquals("John Doe", users.get(0).getName(), "Name should match");
    }

    @Test
    @Timeout(10)
    void testGetUserById() throws IOException {
        // Create a user first
        String jsonInput = "{\"name\":\"John Doe\",\"email\":\"john@example.com\"}";
        HttpResponseData createResponse = sendRequest("POST", "/api/v1/users", jsonInput);
        assertEquals(200, createResponse.statusCode, "Expected HTTP 200 OK for POST setup");
        createdUser = objectMapper.readValue(createResponse.body, UserDto.class);

        HttpResponseData response = sendRequest("GET", "/api/v1/users/" + createdUser.getId(), null);

        assertEquals(200, response.statusCode, "Expected HTTP 200 OK for GET by ID");

        UserDto fetchedUser = objectMapper.readValue(response.body, UserDto.class);
        assertEquals(createdUser.getId(), fetchedUser.getId(), "ID should match");
        assertEquals("John Doe", fetchedUser.getName(), "Name should match");
        assertEquals("john@example.com", fetchedUser.getEmail(), "Email should match");
    }

    @Test
    @Timeout(10)
    void testUpdateUser() throws IOException {
        // Create a user first
        UserDto createDto = new UserDto();
        createDto.setName("Conor");
        createDto.setEmail("conor@mail.com");
        HttpResponseData createResponse = sendRequest("POST", "/api/v1/users", objectMapper.writeValueAsString(createDto));
        assertEquals(200, createResponse.statusCode, "Expected HTTP 200 OK for POST setup");
        createdUser = objectMapper.readValue(createResponse.body, UserDto.class);

        UserDto updateDto = new UserDto();
        updateDto.setName("Updated Conor");
        updateDto.setEmail("updated.conor@mail.com");
        HttpResponseData response = sendRequest("PUT", "/api/v1/users/" + createdUser.getId(), objectMapper.writeValueAsString(updateDto));

        assertEquals(200, response.statusCode, "Expected HTTP 200 OK for PUT");

        UserDto updatedUser = objectMapper.readValue(response.body, UserDto.class);
        assertEquals("Updated Conor", updatedUser.getName(), "Name should be updated");
        assertEquals("updated.conor@mail.com", updatedUser.getEmail(), "Email should be updated");
        assertEquals(createdUser.getId(), updatedUser.getId(), "ID should remain the same");
    }


    @Test
    @Timeout(10)
    void testDeleteUser() throws IOException {
        // Create a user first
        String jsonInput = "{\"name\":\"John Doe\",\"email\":\"john@example.com\"}";
        HttpResponseData createResponse = sendRequest("POST", "/api/v1/users", jsonInput);
        assertEquals(200, createResponse.statusCode, "Expected HTTP 200 OK for POST setup");
        createdUser = objectMapper.readValue(createResponse.body, UserDto.class);

        HttpResponseData response = sendRequest("DELETE", "/api/v1/users/" + createdUser.getId(), null);

        assertEquals(200, response.statusCode, "Expected HTTP 200 OK for DELETE");

        // Verify deletion
        HttpResponseData getResponse = sendRequest("GET", "/api/v1/users/" + createdUser.getId(), null);
        assertEquals(400, getResponse.statusCode, "Expected HTTP 400 Bad Request after deletion");
        assertTrue(getResponse.body.contains("not found"), "Error message should indicate user not found");
    }

    @Test
    @Timeout(10)
    void testInvalidEmailValidation() throws IOException {
        String jsonInput = "{\"name\":\"John Doe\",\"email\":\"invalid\"}";
        HttpResponseData response = sendRequest("POST", "/api/v1/users", jsonInput);

        assertEquals(400, response.statusCode, "Expected HTTP 400 Bad Request for invalid email");
        assertTrue(response.body.contains("Invalid email format"), "Error message should indicate invalid email");
    }

    @Test
    @Timeout(10)
    void testIndexPageServed() throws IOException {
        HttpResponseData response = sendRequest("GET", "/", null);

        assertEquals(200, response.statusCode, "Expected HTTP 200 OK for index page");
        assertTrue(response.body.contains("<html"), "Response should contain '<html'");
    }
}

package com.file_exchange.unit.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.file_exchange.annotations.CustomGetMapping;
import com.file_exchange.annotations.CustomRequestParam;
import com.file_exchange.exceptions.AuthenticationException;
import com.file_exchange.exceptions.NotFoundException;
import com.file_exchange.handlers.HandlerMethod;
import com.file_exchange.handlers.dispatcher.RequestDispatcher;
import com.file_exchange.http.HttpRequest;
import com.file_exchange.http.HttpResponse;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RequestDispatcher tests")
class RequestDispatcherTest {

    private RequestDispatcher dispatcher;
    private TestController testController;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        testController = new TestController();
        Map<String, HandlerMethod> routes = new HashMap<>();

        Method helloMethod = TestController.class.getMethod("hello");
        routes.put("GET:/api/hello", new HandlerMethod(testController, helloMethod, "/api/hello", "GET"));

        Method echoMethod = TestController.class.getMethod("echo", String.class);
        routes.put("GET:/api/echo", new HandlerMethod(testController, echoMethod, "/api/echo", "GET"));

        Method errorMethod = TestController.class.getMethod("error");
        routes.put("GET:/api/error", new HandlerMethod(testController, errorMethod, "/api/error", "GET"));

        Method badRequestMethod = TestController.class.getMethod("badRequest");
        routes.put(
                "GET:/api/badrequest", new HandlerMethod(testController, badRequestMethod, "/api/badrequest", "GET"));

        Method notFoundMethod = TestController.class.getMethod("notFound");
        routes.put("GET:/api/notfound", new HandlerMethod(testController, notFoundMethod, "/api/notfound", "GET"));

        Method unauthorizedMethod = TestController.class.getMethod("unauthorized");
        routes.put(
                "GET:/api/unauthorized",
                new HandlerMethod(testController, unauthorizedMethod, "/api/unauthorized", "GET"));

        dispatcher = new RequestDispatcher(routes, new ObjectMapper());
    }

    @Test
    @DisplayName("Should handle GET request and return string response")
    void testHandleGetRequest() {
        HttpRequest request = createRequest("GET", "/api/hello", "", Map.of(), Map.of());

        HttpResponse response = dispatcher.handleRequest(request);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(new String(response.getBody())).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("Should return 404 for unknown route with JSON body")
    void testHandleUnknownRoute() {
        HttpRequest request = createRequest("GET", "/api/unknown", "", Map.of(), Map.of());

        HttpResponse response = dispatcher.handleRequest(request);

        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(new String(response.getBody())).contains("\"status\":404");
    }

    @Test
    @DisplayName("Should bind query parameters")
    void testBindQueryParams() {
        HttpRequest request = createRequest("GET", "/api/echo", "", Map.of(), Map.of("message", "test"));

        HttpResponse response = dispatcher.handleRequest(request);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(new String(response.getBody())).isEqualTo("Echo: test");
    }

    @Test
    @DisplayName("Should return 500 for server error with JSON body")
    void testHandleServerError() {
        HttpRequest request = createRequest("GET", "/api/error", "", Map.of(), Map.of());

        HttpResponse response = dispatcher.handleRequest(request);

        assertThat(response.getStatusCode()).isEqualTo(500);
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(new String(response.getBody())).contains("\"status\":500");
    }

    @Test
    @DisplayName("Should return 400 for IllegalArgumentException with JSON body")
    void testHandleBadRequest() {
        HttpRequest request = createRequest("GET", "/api/badrequest", "", Map.of(), Map.of());

        HttpResponse response = dispatcher.handleRequest(request);

        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(new String(response.getBody())).contains("\"error\":\"Invalid input\"");
    }

    @Test
    @DisplayName("Should return 404 for NotFoundException with JSON body")
    void testHandleNotFoundException() {
        HttpRequest request = createRequest("GET", "/api/notfound", "", Map.of(), Map.of());

        HttpResponse response = dispatcher.handleRequest(request);

        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(new String(response.getBody())).contains("\"error\":\"Resource not found\"");
    }

    @Test
    @DisplayName("Should return 401 for AuthenticationException with JSON body")
    void testHandleAuthenticationException() {
        HttpRequest request = createRequest("GET", "/api/unauthorized", "", Map.of(), Map.of());

        HttpResponse response = dispatcher.handleRequest(request);

        assertThat(response.getStatusCode()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(new String(response.getBody())).contains("\"error\":\"Invalid credentials\"");
    }

    @Test
    @DisplayName("Should return 404 for wrong HTTP method with JSON body")
    void testWrongHttpMethod() {
        HttpRequest request = createRequest("POST", "/api/hello", "", Map.of(), Map.of());

        HttpResponse response = dispatcher.handleRequest(request);

        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThat(response.getContentType()).isEqualTo("application/json");
    }

    private HttpRequest createRequest(
            String method, String path, String body, Map<String, String> headers, Map<String, String> queryParams) {
        return new HttpRequest(method, path, headers, body, new HashMap<>(), queryParams);
    }

    public static class TestController {
        @CustomGetMapping("/api/hello")
        public String hello() {
            return "Hello World";
        }

        @CustomGetMapping("/api/echo")
        public String echo(@CustomRequestParam("message") String message) {
            return "Echo: " + message;
        }

        @CustomGetMapping("/api/error")
        public String error() {
            throw new RuntimeException("Something went wrong");
        }

        @CustomGetMapping("/api/badrequest")
        public String badRequest() {
            throw new IllegalArgumentException("Invalid input");
        }

        @CustomGetMapping("/api/notfound")
        public String notFound() {
            throw new NotFoundException("Resource not found");
        }

        @CustomGetMapping("/api/unauthorized")
        public String unauthorized() {
            throw new AuthenticationException("Invalid credentials");
        }
    }
}

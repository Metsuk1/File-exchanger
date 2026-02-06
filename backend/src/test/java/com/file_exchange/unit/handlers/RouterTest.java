package com.file_exchange.unit.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import com.file_exchange.handlers.HandlerMethod;
import com.file_exchange.handlers.dispatcher.Router;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Router tests")
class RouterTest {

    private Router router;
    private HandlerMethod testHandler;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        Method method = RouterTest.class.getDeclaredMethod("dummyMethod");
        testHandler = new HandlerMethod(this, method, "/test", "GET");
    }

    @Test
    @DisplayName("Should find exact match handler")
    void testExactMatch() {
        Map<String, HandlerMethod> routes = new HashMap<>();
        routes.put("GET:/api/v1/users", testHandler);
        router = new Router(routes);

        HandlerMethod result = router.findHandler("GET", "/api/v1/users");

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testHandler);
    }

    @Test
    @DisplayName("Should return null for non-matching route")
    void testNoMatch() {
        Map<String, HandlerMethod> routes = new HashMap<>();
        routes.put("GET:/api/v1/users", testHandler);
        router = new Router(routes);

        HandlerMethod result = router.findHandler("GET", "/api/v1/files");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null for non-matching method")
    void testMethodMismatch() {
        Map<String, HandlerMethod> routes = new HashMap<>();
        routes.put("GET:/api/v1/users", testHandler);
        router = new Router(routes);

        HandlerMethod result = router.findHandler("POST", "/api/v1/users");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should find handler with path variable")
    void testPathVariableMatch() {
        Map<String, HandlerMethod> routes = new HashMap<>();
        routes.put("GET:/api/v1/users/{id}", testHandler);
        router = new Router(routes);

        HandlerMethod result = router.findHandler("GET", "/api/v1/users/123");

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should strip query params when matching")
    void testQueryParamsStripped() {
        Map<String, HandlerMethod> routes = new HashMap<>();
        routes.put("GET:/api/v1/files", testHandler);
        router = new Router(routes);

        HandlerMethod result = router.findHandler("GET", "/api/v1/files?fileId=123");

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should handle empty path")
    void testEmptyPath() {
        Map<String, HandlerMethod> routes = new HashMap<>();
        routes.put("GET:/", testHandler);
        router = new Router(routes);

        HandlerMethod result = router.findHandler("GET", "");

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should not match different segment count")
    void testDifferentSegmentCount() {
        Map<String, HandlerMethod> routes = new HashMap<>();
        routes.put("GET:/api/v1/users/{id}", testHandler);
        router = new Router(routes);

        HandlerMethod result = router.findHandler("GET", "/api/v1/users");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle null path")
    void testNullPath() {
        Map<String, HandlerMethod> routes = new HashMap<>();
        routes.put("GET:/", testHandler);
        router = new Router(routes);

        HandlerMethod result = router.findHandler("GET", null);

        assertThat(result).isNotNull();
    }

    // Dummy method for creating HandlerMethod
    public void dummyMethod() {}
}

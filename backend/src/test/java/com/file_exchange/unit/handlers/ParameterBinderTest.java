package com.file_exchange.unit.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.file_exchange.annotations.*;
import com.file_exchange.handlers.dispatcher.ParameterBinder;
import com.file_exchange.http.HttpRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ParameterBinder tests")
class ParameterBinderTest {

    private ParameterBinder parameterBinder;

    @BeforeEach
    void setUp() {
        parameterBinder = new ParameterBinder(new ObjectMapper());
    }

    @Test
    @DisplayName("Should bind request body to object")
    void testBindRequestBody() throws Exception {
        Method method = TestController.class.getMethod("bodyMethod", TestDto.class);
        HttpRequest request = createRequest("POST", "/test", "{\"name\":\"John\",\"age\":25}", Map.of(), Map.of());

        Object[] args = parameterBinder.bindParameters(method, request, "/test");

        assertThat(args).hasSize(1);
        assertThat(args[0]).isInstanceOf(TestDto.class);
        TestDto dto = (TestDto) args[0];
        assertThat(dto.name).isEqualTo("John");
        assertThat(dto.age).isEqualTo(25);
    }

    @Test
    @DisplayName("Should bind request body as string")
    void testBindRequestBodyAsString() throws Exception {
        Method method = TestController.class.getMethod("stringBodyMethod", String.class);
        HttpRequest request = createRequest("POST", "/test", "plain text body", Map.of(), Map.of());

        Object[] args = parameterBinder.bindParameters(method, request, "/test");

        assertThat(args).hasSize(1);
        assertThat(args[0]).isEqualTo("plain text body");
    }

    @Test
    @DisplayName("Should bind empty body to null")
    void testBindEmptyBody() throws Exception {
        Method method = TestController.class.getMethod("bodyMethod", TestDto.class);
        HttpRequest request = createRequest("POST", "/test", "", Map.of(), Map.of());

        Object[] args = parameterBinder.bindParameters(method, request, "/test");

        assertThat(args).hasSize(1);
        assertThat(args[0]).isNull();
    }

    @Test
    @DisplayName("Should bind query parameter as string")
    void testBindQueryParamString() throws Exception {
        Method method = TestController.class.getMethod("queryMethod", String.class);
        HttpRequest request = createRequest("GET", "/test", "", Map.of(), Map.of("name", "John"));

        Object[] args = parameterBinder.bindParameters(method, request, "/test");

        assertThat(args).hasSize(1);
        assertThat(args[0]).isEqualTo("John");
    }

    @Test
    @DisplayName("Should bind query parameter as Long")
    void testBindQueryParamLong() throws Exception {
        Method method = TestController.class.getMethod("queryMethodLong", Long.class);
        HttpRequest request = createRequest("GET", "/test", "", Map.of(), Map.of("id", "123"));

        Object[] args = parameterBinder.bindParameters(method, request, "/test");

        assertThat(args).hasSize(1);
        assertThat(args[0]).isEqualTo(123L);
    }

    @Test
    @DisplayName("Should bind query parameter as Integer")
    void testBindQueryParamInt() throws Exception {
        Method method = TestController.class.getMethod("queryMethodInt", Integer.class);
        HttpRequest request = createRequest("GET", "/test", "", Map.of(), Map.of("count", "42"));

        Object[] args = parameterBinder.bindParameters(method, request, "/test");

        assertThat(args).hasSize(1);
        assertThat(args[0]).isEqualTo(42);
    }

    @Test
    @DisplayName("Should bind query parameter as Double")
    void testBindQueryParamDouble() throws Exception {
        Method method = TestController.class.getMethod("queryMethodDouble", Double.class);
        HttpRequest request = createRequest("GET", "/test", "", Map.of(), Map.of("price", "19.99"));

        Object[] args = parameterBinder.bindParameters(method, request, "/test");

        assertThat(args).hasSize(1);
        assertThat(args[0]).isEqualTo(19.99);
    }

    @Test
    @DisplayName("Should bind query parameter as Boolean")
    void testBindQueryParamBoolean() throws Exception {
        Method method = TestController.class.getMethod("queryMethodBoolean", Boolean.class);
        HttpRequest request = createRequest("GET", "/test", "", Map.of(), Map.of("active", "true"));

        Object[] args = parameterBinder.bindParameters(method, request, "/test");

        assertThat(args).hasSize(1);
        assertThat(args[0]).isEqualTo(true);
    }

    @Test
    @DisplayName("Should return null for missing query parameter")
    void testBindMissingQueryParam() throws Exception {
        Method method = TestController.class.getMethod("queryMethod", String.class);
        HttpRequest request = createRequest("GET", "/test", "", Map.of(), Map.of());

        Object[] args = parameterBinder.bindParameters(method, request, "/test");

        assertThat(args).hasSize(1);
        assertThat(args[0]).isNull();
    }

    @Test
    @DisplayName("Should bind path variable")
    void testBindPathVariable() throws Exception {
        Method method = TestController.class.getMethod("pathVarMethod", Long.class);
        HttpRequest request = createRequest("GET", "/users/456", "", Map.of(), Map.of());

        Object[] args = parameterBinder.bindParameters(method, request, "/users/{id}");

        assertThat(args).hasSize(1);
        assertThat(args[0]).isEqualTo(456L);
    }

    @Test
    @DisplayName("Should bind request header")
    void testBindRequestHeader() throws Exception {
        Method method = TestController.class.getMethod("headerMethod", String.class);
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer token123");
        HttpRequest request = createRequest("GET", "/test", "", headers, Map.of());

        Object[] args = parameterBinder.bindParameters(method, request, "/test");

        assertThat(args).hasSize(1);
        assertThat(args[0]).isEqualTo("Bearer token123");
    }

    @Test
    @DisplayName("Should bind multiple parameters")
    void testBindMultipleParameters() throws Exception {
        Method method = TestController.class.getMethod("multiParamMethod", Long.class, String.class);
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer xyz");
        HttpRequest request = createRequest("GET", "/users/789?name=test", "", headers, Map.of("name", "test"));

        Object[] args = parameterBinder.bindParameters(method, request, "/users/{id}");

        assertThat(args).hasSize(2);
        assertThat(args[0]).isEqualTo(789L);
        assertThat(args[1]).isEqualTo("test");
    }

    @Test
    @DisplayName("Should handle path with query params")
    void testPathWithQueryParams() throws Exception {
        Method method = TestController.class.getMethod("pathVarMethod", Long.class);
        HttpRequest request = createRequest("GET", "/users/123?extra=value", "", Map.of(), Map.of("extra", "value"));

        Object[] args = parameterBinder.bindParameters(method, request, "/users/{id}");

        assertThat(args).hasSize(1);
        assertThat(args[0]).isEqualTo(123L);
    }

    @Test
    @DisplayName("Should handle mismatched path lengths")
    void testMismatchedPathLengths() throws Exception {
        Method method = TestController.class.getMethod("pathVarMethod", Long.class);
        HttpRequest request = createRequest("GET", "/users", "", Map.of(), Map.of());

        Object[] args = parameterBinder.bindParameters(method, request, "/users/{id}");

        assertThat(args).hasSize(1);
        assertThat(args[0]).isNull();
    }

    private HttpRequest createRequest(
            String method, String path, String body, Map<String, String> headers, Map<String, String> queryParams) {
        return new HttpRequest(method, path, headers, body, new HashMap<>(), queryParams);
    }

    // Test controller class with annotated methods
    public static class TestController {
        public void bodyMethod(@CustomRequestBody TestDto dto) {}

        public void stringBodyMethod(@CustomRequestBody String body) {}

        public void queryMethod(@CustomRequestParam("name") String name) {}

        public void queryMethodLong(@CustomRequestParam("id") Long id) {}

        public void queryMethodInt(@CustomRequestParam("count") Integer count) {}

        public void queryMethodDouble(@CustomRequestParam("price") Double price) {}

        public void queryMethodBoolean(@CustomRequestParam("active") Boolean active) {}

        public void pathVarMethod(@CustomPathVariable("id") Long id) {}

        public void headerMethod(@CustomRequestHeader("Authorization") String auth) {}

        public void multiParamMethod(@CustomPathVariable("id") Long id, @CustomRequestParam("name") String name) {}
    }

    public static class TestDto {
        public String name;
        public int age;
    }
}

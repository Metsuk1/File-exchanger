package com.file_exchange.handlers.dispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.file_exchange.dto.ErrorResponse;
import com.file_exchange.exceptions.AppException;
import com.file_exchange.handlers.HandlerMethod;
import com.file_exchange.http.HttpRequest;
import com.file_exchange.http.HttpResponse;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * // Entry point class that orchestrates request handling (SRP: Coordination only).
 */
public class RequestDispatcher {
    private static final Logger log = LoggerFactory.getLogger(RequestDispatcher.class);

    private final Router router;
    private final ParameterBinder parameterBinder;
    private final ResponseConverter responseConverter;
    private final ObjectMapper objectMapper;

    public RequestDispatcher(Map<String, HandlerMethod> routeHandlers, ObjectMapper objectMapper) {
        this.router = new Router(routeHandlers);
        this.parameterBinder = new ParameterBinder(objectMapper);
        this.responseConverter = new ResponseConverter(objectMapper);
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    public HttpResponse handleRequest(HttpRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return addCorsHeaders(HttpResponse.noContent());
        }

        try {
            // Find matching handler
            HandlerMethod handler = router.findHandler(request.getMethod(), request.getPath());
            if (handler == null) {
                return addCorsHeaders(buildErrorResponse(404, "Not Found"));
            }
            // Bind parameters and invoke method
            Object[] args = parameterBinder.bindParameters(handler.getMethod(), request, handler.getPath());
            Object result = handler.getMethod().invoke(handler.getController(), args);
            // Convert result to response
            return addCorsHeaders(responseConverter.convertToResponse(result));
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof AppException appEx) {
                return addCorsHeaders(buildErrorResponse(appEx.getStatusCode(), appEx.getMessage()));
            }
            if (cause instanceof IllegalArgumentException) {
                return addCorsHeaders(buildErrorResponse(400, cause.getMessage()));
            }
            log.error("Handler invocation failed", cause);
            return addCorsHeaders(buildErrorResponse(500, "Internal Server Error"));
        } catch (IllegalArgumentException e) {
            return addCorsHeaders(buildErrorResponse(400, e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error handling request", e);
            return addCorsHeaders(buildErrorResponse(500, "Internal Server Error"));
        }
    }

    private HttpResponse addCorsHeaders(HttpResponse response) {
        return response.withHeader("Access-Control-Allow-Origin", "*")
                .withHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS")
                .withHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
                .withHeader("Access-Control-Max-Age", "86400");
    }

    @SneakyThrows
    private HttpResponse buildErrorResponse(int statusCode, String message) {
        ErrorResponse errorResponse = new ErrorResponse(message, statusCode);
        String json = objectMapper.writeValueAsString(errorResponse);
        String statusText =
                switch (statusCode) {
                    case 400 -> "Bad Request";
                    case 401 -> "Unauthorized";
                    case 404 -> "Not Found";
                    default -> "Internal Server Error";
                };
        return HttpResponse.jsonError(statusCode, statusText, json);
    }
}

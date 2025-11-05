package com.file_exchange.handlers.dispatcher;

import com.file_exchange.handlers.HandlerMethod;
import com.file_exchange.http.HttpRequest;
import com.file_exchange.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * // Entry point class that orchestrates request handling (SRP: Coordination only).
 */
public class RequestDispatcher {
    private final Router router;
    private final ParameterBinder parameterBinder;
    private final ResponseConverter responseConverter;

    public RequestDispatcher(Map<String, HandlerMethod> routeHandlers, ObjectMapper objectMapper) {
        this.router = new Router(routeHandlers);
        this.parameterBinder = new ParameterBinder(objectMapper);
        this.responseConverter = new ResponseConverter(objectMapper);
    }

    @SneakyThrows
    public HttpResponse handleRequest(HttpRequest request) {
        try {
            // Find matching handler
            HandlerMethod handler = router.findHandler(request.getMethod(), request.getPath());
            if (handler == null) {
                return HttpResponse.notFound();
            }
            // Bind parameters and invoke method
            Object[] args = parameterBinder.bindParameters(handler.getMethod(), request, handler.getPath());
            Object result = handler.getMethod().invoke(handler.getController(), args);
            // Convert result to response
            return responseConverter.convertToResponse(result);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                return HttpResponse.badRequest(cause.getMessage());
            }
            cause.printStackTrace();
            return HttpResponse.serverError();
        } catch (IllegalArgumentException e) {
            return HttpResponse.badRequest("Invalid parameter types: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return HttpResponse.serverError();
        }
    }
}

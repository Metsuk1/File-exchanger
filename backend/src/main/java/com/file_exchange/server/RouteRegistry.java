package com.file_exchange.server;

import com.file_exchange.annotations.*;
import com.file_exchange.handlers.HandlerMethod;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteRegistry {
    private static final Logger log = LoggerFactory.getLogger(RouteRegistry.class);

    private final Map<String, HandlerMethod> routeHandlers = new ConcurrentHashMap<>();

    public void registerController(Object controller) {
        Class<?> clas = controller.getClass();
        if (!clas.isAnnotationPresent(CustomRestController.class)) {
            log.warn("{} is not a @CustomRestController", clas.getName());
            return;
        }

        String basePath = "";
        if (clas.isAnnotationPresent(CustomRequestMapping.class)) {
            basePath = clas.getAnnotation(CustomRequestMapping.class).value();
            if (!basePath.startsWith("/")) basePath = "/" + basePath;
        }

        for (Method method : clas.getDeclaredMethods()) {
            String path = basePath;
            String httpMethod = null;

            if (method.isAnnotationPresent(CustomRequestMapping.class)) {
                CustomRequestMapping mapping = method.getAnnotation(CustomRequestMapping.class);
                path = combinePaths(basePath, mapping.value());
                httpMethod = mapping.httpMethod().name();
            } else if (method.isAnnotationPresent(CustomGetMapping.class)) {
                CustomGetMapping mapping = method.getAnnotation(CustomGetMapping.class);
                path = combinePaths(basePath, mapping.value());
                httpMethod = "GET";
            } else if (method.isAnnotationPresent(CustomPostMapping.class)) {
                CustomPostMapping mapping = method.getAnnotation(CustomPostMapping.class);
                path = combinePaths(basePath, mapping.value());
                httpMethod = "POST";
            } else if (method.isAnnotationPresent(CustomPutMapping.class)) {
                CustomPutMapping mapping = method.getAnnotation(CustomPutMapping.class);
                path = combinePaths(basePath, mapping.value());
                httpMethod = "PUT";
            } else if (method.isAnnotationPresent(CustomPatchMapping.class)) {
                CustomPatchMapping mapping = method.getAnnotation(CustomPatchMapping.class);
                path = combinePaths(basePath, mapping.value());
                httpMethod = "PATCH";
            } else if (method.isAnnotationPresent(CustomDeleteMapping.class)) {
                CustomDeleteMapping mapping = method.getAnnotation(CustomDeleteMapping.class);
                path = combinePaths(basePath, mapping.value());
                httpMethod = "DELETE";
            }

            if (httpMethod != null) {
                String key = httpMethod + ":" + path;
                routeHandlers.put(key, new HandlerMethod(controller, method, path, httpMethod));
            }
        }
    }

    public void register(String key, HandlerMethod handler) {
        routeHandlers.put(key, handler);
    }

    public HandlerMethod getHandler(String key) {
        return routeHandlers.get(key);
    }

    public Set<Map.Entry<String, HandlerMethod>> entrySet() {
        return routeHandlers.entrySet();
    }

    private String combinePaths(String basePath, String methodPath) {
        String path = basePath;
        if (!methodPath.isEmpty()) {
            if (!methodPath.startsWith("/")) {
                path += "/" + methodPath;
            } else {
                path += methodPath;
            }
        }
        return path.isEmpty() ? "/" : path;
    }
}

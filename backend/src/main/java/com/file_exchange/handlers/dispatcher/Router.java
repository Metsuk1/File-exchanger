package com.file_exchange.handlers.dispatcher;

import com.file_exchange.handlers.HandlerMethod;
import com.file_exchange.server.RouteRegistry;
import java.util.Map;

/**
 * Handles route matching, including path variables.
 */
public class Router {
    private final RouteRegistry routeRegistry;

    public Router(RouteRegistry routeRegistry) {
        this.routeRegistry = routeRegistry;
    }

    public HandlerMethod findHandler(String method, String rawPath) {
        String path = extractPathWithoutQuery(rawPath);
        String key = method + ":" + path;
        HandlerMethod handler = routeRegistry.getHandler(key);
        if (handler != null) {
            return handler;
        }
        // Check for path variable matches
        for (Map.Entry<String, HandlerMethod> entry : routeRegistry.entrySet()) {
            String routeKey = entry.getKey();
            String routeMethod = extractMethodFromKey(routeKey);
            String routePath = extractPathFromKey(routeKey);
            if (matchesPath(method, path, routeMethod, routePath)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String extractPathWithoutQuery(String rawPath) {
        if (rawPath == null || rawPath.isEmpty()) {
            return "/";
        }
        return rawPath.contains("?") ? rawPath.substring(0, rawPath.indexOf("?")) : rawPath;
    }

    private String extractMethodFromKey(String key) {
        return key.substring(0, key.indexOf(":"));
    }

    private String extractPathFromKey(String key) {
        return key.substring(key.indexOf(":") + 1);
    }

    private boolean matchesPath(String requestMethod, String requestPath, String routeMethod, String routePath) {
        if (!requestMethod.equals(routeMethod)) {
            return false;
        }

        if (requestPath.isEmpty()) requestPath = "/";
        if (routePath.isEmpty()) routePath = "/";

        String[] requestParts = requestPath.split("/");
        String[] routeParts = routePath.split("/");
        if (requestParts.length != routeParts.length) {
            return false;
        }
        for (int i = 0; i < routeParts.length; i++) {
            if (isPathVariable(routeParts[i])) {
                continue; // Accept any value for variables
            }
            if (!routeParts[i].equals(requestParts[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean isPathVariable(String part) {
        return part.startsWith("{") && part.endsWith("}");
    }
}

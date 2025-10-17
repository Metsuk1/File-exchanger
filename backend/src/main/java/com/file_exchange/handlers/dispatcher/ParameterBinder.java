package com.file_exchange.handlers.dispatcher;

import com.file_exchange.annotations.*;
import com.file_exchange.handlers.utilsFiles.TempFileInputStream;
import com.file_exchange.http.HttpRequest;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Binds parameters to method arguments using annotations
 */
public class ParameterBinder {
    private final ObjectMapper objectMapper;

    public ParameterBinder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Object[] bindParameters(Method method, HttpRequest request,String mappingPath) throws IOException {
        var params = method.getParameters();
        Object[] args = new Object[params.length];
        Map<String, String> queryParams = parseQueryParams(request.getPath(), request.getBody());
        Map<String, String> pathVariables = extractPathVariables(mappingPath, request.getPath());

        for (int i = 0; i < params.length; i++) {
            if (params[i].isAnnotationPresent(CustomRequestBody.class)) {
                args[i] = bindBody(request.getBody(), params[i].getType());
            } else if (params[i].isAnnotationPresent(CustomRequestParam.class)) {
                String name = params[i].getAnnotation(CustomRequestParam.class).value();
                args[i] = convertValue(queryParams.get(name), params[i].getType());
            } else if (params[i].isAnnotationPresent(CustomPathVariable.class)) {
                String name = params[i].getAnnotation(CustomPathVariable.class).value();
                args[i] = convertValue(pathVariables.get(name), params[i].getType());
            }else if (params[i].isAnnotationPresent(CustomRequestHeader.class)){
                String name = params[i].getAnnotation(CustomRequestHeader.class).value().toLowerCase(); // .toLowerCase()
                args[i] = request.getHeaders().get(name);
            }else if (params[i].isAnnotationPresent(CustomRequestPart.class)) {
                String name = params[i].getAnnotation(CustomRequestPart.class).value();

                if (params[i].getType() == InputStream.class) {
                    args[i] = request.getPartAsStream(name);
                } else if (params[i].getType() == String.class) {
                    args[i] = request.getPartAsString(name);
                } else if (params[i].getType() == Long.class || params[i].getType() == long.class) {
                    args[i] = request.getPartAsLong(name);
                }else if (params[i].getType() == TempFileInputStream.class){
                    args[i] = request.getPartAsTempFile(name);
                } else {
                    throw new IllegalArgumentException("Unsupported type for @CustomRequestPart: " + params[i].getType().getName());
                }
            }
        }

        return args;
    }

    private Object bindBody(String body, Class<?> type) throws IOException {
        if (body == null || body.isEmpty()) {
            return null;
        }
        if (type == String.class) {
            return body;
        }
        return objectMapper.readValue(body, type);
    }

    private Object convertValue(String value, Class<?> type) {
        if (value == null) {
            return null;
        }
        if (type == Integer.class || type == int.class) {
            return Integer.parseInt(value);
        } else if (type == Long.class || type == long.class) {
            return Long.parseLong(value);
        } else if (type == Double.class || type == double.class) {
            return Double.parseDouble(value);
        } else if (type == Boolean.class || type == boolean.class) {
            return Boolean.parseBoolean(value);
        }
        return value;
    }

    private Map<String, String> parseQueryParams(String path,String body) {
        Map<String, String> params = new HashMap<>();
        // Parse query string
        if (path.contains("?")) {
            String query = path.substring(path.indexOf("?") + 1);
            params.putAll(parsePairs(query));
        }
        // Parse form-urlencoded body
        if (body != null && !body.isEmpty() && body.contains("=")) {
            params.putAll(parsePairs(body));
        }

        return params;
    }

    private Map<String, String> parsePairs(String query) {
        Map<String, String> params = new HashMap<>();
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                params.put(key, value);
            }
        }
        return params;
    }

    private Map<String, String> extractPathVariables(String mappingPath, String requestPath) {
        Map<String, String> variables = new HashMap<>();
        String[] mappingParts = mappingPath.split("/");
        String cleanRequestPath = extractPathWithoutQuery(requestPath);
        String[] requestParts = cleanRequestPath.split("/");

        if (mappingParts.length != requestParts.length) {
            return variables;
        }

        for (int i = 0; i < mappingParts.length; i++) {
            if (mappingParts[i].startsWith("{") && mappingParts[i].endsWith("}")) {
                String varName = mappingParts[i].substring(1, mappingParts[i].length() - 1);
                variables.put(varName, requestParts[i]);
            }
        }
        return variables;
    }

    private String extractPathWithoutQuery(String rawPath) {
        return rawPath.contains("?") ? rawPath.substring(0, rawPath.indexOf("?")) : rawPath;
    }
}

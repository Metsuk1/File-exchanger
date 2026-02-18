package com.file_exchange.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.file_exchange.annotations.*;
import com.file_exchange.executor.CustomExecutorService;
import com.file_exchange.handlers.HandlerMethod;
import com.file_exchange.handlers.dispatcher.HttpRequestParser;
import com.file_exchange.handlers.dispatcher.RequestDispatcher;
import com.file_exchange.http.HttpRequest;
import com.file_exchange.http.HttpResponse;
import java.io.*;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomWebServer {
    private static final Logger log = LoggerFactory.getLogger(CustomWebServer.class);

    private final int port;
    private final CustomExecutorService executor;
    private ServerSocket serverSocket;
    private volatile boolean running = false;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpRequestParser requestParser;
    private final RequestDispatcher requestDispatcher;
    private long requestCount = 0;
    private final Map<String, HandlerMethod> routeHandlers = new ConcurrentHashMap<>();

    public CustomWebServer(int port, int threadPoolSize, boolean useVirtualThreads) {
        this.port = port == 0 ? Integer.parseInt(System.getenv("PORT") != null ? System.getenv("PORT") : "8080") : port;
        this.executor = useVirtualThreads
                ? CustomExecutorService.newVirtualThreadPool(threadPoolSize)
                : CustomExecutorService.newPlatformThreadPool(threadPoolSize);
        this.requestParser = new HttpRequestParser();
        this.requestDispatcher = new RequestDispatcher(routeHandlers, objectMapper);
        this.running = false;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        executor.execute(this::serverLoop);
    }

    private void serverLoop() {
        while (running && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                executor.execute(() -> handleClient(clientSocket));
            } catch (Exception e) {
                if (running) {
                    log.error("Error accepting client connection", e);
                }
            }
        }
    }

    @SneakyThrows
    private void handleClient(Socket clientSocket) {
        boolean keepAlive = true;
        try (InputStream in = clientSocket.getInputStream();
                OutputStream out = clientSocket.getOutputStream()) {

            clientSocket.setSoTimeout(30000);

            while (keepAlive && running) {
                HttpRequest request = requestParser.parse(in);
                if (request == null) break;

                HttpResponse response = requestDispatcher.handleRequest(request);
                requestCount++;
                if (response == null) break;

                String connectionHeader =
                        request.getHeaders().getOrDefault("connection", "").toLowerCase();

                if ("close".equals(connectionHeader) || response.getStatusCode() >= 400) {
                    keepAlive = false;
                }

                sendResponse(out, response, keepAlive);
            }

        } catch (Exception e) {
            if (running) {
                log.error("Error while handling client: {}", e.getMessage(), e);
            }
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                log.error("Socket close failed (will be ignored): {}", e.getMessage());
            }
        }
    }

    @SneakyThrows
    private void sendResponse(OutputStream out, HttpResponse response, boolean keepAlive) {
        PrintWriter writer = new PrintWriter(out);
        writer.printf("HTTP/1.1 %d %s\r\n", response.getStatusCode(), response.getStatusText());
        writer.printf("Content-Type: %s\r\n", response.getContentType());
        writer.printf("Content-Length: %d\r\n", response.getBody().length);
        writer.printf("Connection: %s\r\n", keepAlive ? "keep-alive" : "close");

        if (response.getHeaders() != null) {
            for (Map.Entry<String, String> h : response.getHeaders().entrySet()) {
                writer.printf("%s: %s\r\n", h.getKey(), h.getValue());
            }
        }
        writer.print("\r\n");
        writer.flush();
        if (response.getBody() != null && response.getBody().length > 0) {
            out.write(response.getBody());
            out.flush();
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log.error("Error closing server socket: {}", e.getMessage(), e);
        } finally {
            executor.shutdown();
        }
    }

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

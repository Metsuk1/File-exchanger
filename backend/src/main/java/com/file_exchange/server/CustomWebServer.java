package com.file_exchange.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.file_exchange.executor.CustomExecutorService;
import com.file_exchange.handlers.dispatcher.HttpRequestParser;
import com.file_exchange.handlers.dispatcher.RequestDispatcher;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomWebServer {
    private static final Logger log = LoggerFactory.getLogger(CustomWebServer.class);

    private final int port;
    private final CustomExecutorService executor;
    private final ClientConnectionHandler connectionHandler;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ServerSocket serverSocket;

    public CustomWebServer(int port, int threadPoolSize, boolean useVirtualThreads, RouteRegistry routeRegistry) {
        this.port = port == 0 ? Integer.parseInt(System.getenv("PORT") != null ? System.getenv("PORT") : "8080") : port;
        this.executor = useVirtualThreads
                ? CustomExecutorService.newVirtualThreadPool(threadPoolSize)
                : CustomExecutorService.newPlatformThreadPool(threadPoolSize);
        HttpRequestParser requestParser = new HttpRequestParser();
        RequestDispatcher requestDispatcher = new RequestDispatcher(routeRegistry, new ObjectMapper());
        this.connectionHandler = new ClientConnectionHandler(requestParser, requestDispatcher, running);
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running.set(true);
        executor.execute(this::serverLoop);
    }

    private void serverLoop() {
        while (running.get() && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                executor.execute(() -> connectionHandler.handle(clientSocket));
            } catch (Exception e) {
                if (running.get()) {
                    log.error("Error accepting client connection", e);
                }
            }
        }
    }

    public void stop() {
        running.set(false);
        closeServerSocket();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                log.warn("Forced shutdown — some requests may not have completed");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("Server stopped");
    }

    private void closeServerSocket() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log.error("Error closing server socket: {}", e.getMessage(), e);
        }
    }
}

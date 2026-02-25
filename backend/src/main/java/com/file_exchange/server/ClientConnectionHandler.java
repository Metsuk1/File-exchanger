package com.file_exchange.server;

import com.file_exchange.handlers.dispatcher.HttpRequestParser;
import com.file_exchange.handlers.dispatcher.RequestDispatcher;
import com.file_exchange.http.HttpRequest;
import com.file_exchange.http.HttpResponse;
import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientConnectionHandler {
    private static final Logger log = LoggerFactory.getLogger(ClientConnectionHandler.class);

    private final HttpRequestParser requestParser;
    private final RequestDispatcher requestDispatcher;
    private final AtomicBoolean running;
    private final AtomicLong requestCount = new AtomicLong(0);

    public ClientConnectionHandler(
            HttpRequestParser requestParser, RequestDispatcher requestDispatcher, AtomicBoolean running) {
        this.requestParser = requestParser;
        this.requestDispatcher = requestDispatcher;
        this.running = running;
    }

    @SneakyThrows
    public void handle(Socket clientSocket) {
        boolean keepAlive = true;
        try (InputStream in = clientSocket.getInputStream();
                OutputStream out = clientSocket.getOutputStream()) {

            clientSocket.setSoTimeout(30000);

            while (keepAlive && running.get()) {
                HttpRequest request = requestParser.parse(in);
                if (request == null) break;

                HttpResponse response = requestDispatcher.handleRequest(request);
                requestCount.incrementAndGet();
                if (response == null) break;

                String connectionHeader =
                        request.getHeaders().getOrDefault("connection", "").toLowerCase();

                if ("close".equals(connectionHeader) || response.getStatusCode() >= 400) {
                    keepAlive = false;
                }

                sendResponse(out, response, keepAlive);
            }

        } catch (Exception e) {
            if (running.get()) {
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
}

package com.file_exchange.handlers.dispatcher;

import com.file_exchange.http.HttpResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * // Serves static files securely
 */
public class StaticFileServer {
    private final Path staticRoot;

    public StaticFileServer(String staticResourcesPath) {
        this.staticRoot = Paths.get(staticResourcesPath).toAbsolutePath().normalize();
    }

    public HttpResponse serve(String rawPath) throws IOException {
        String path = rawPath.equals("/") ? "/index.html" : rawPath;
        Path requestedPath = staticRoot.resolve(path.substring(1)).normalize();
        // Security: Prevent path traversal
        if (!requestedPath.startsWith(staticRoot)) {
            return HttpResponse.forbidden();
        }
        if (!Files.exists(requestedPath) || !Files.isRegularFile(requestedPath)) {
            return null; // Not found or not a file
        }
        byte[] content = Files.readAllBytes(requestedPath);
        String mimeType = Files.probeContentType(requestedPath);
        return HttpResponse.ok(content, mimeType != null ? mimeType : "text/html");
    }

}

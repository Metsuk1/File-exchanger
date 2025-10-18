package com.file_exchange.handlers.utilsFiles;

public class MimeTypeUtils {
    public static String detect(String filename) {
        if (filename == null) return "application/octet-stream";
        String name = filename.toLowerCase();
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".pdf")) return "application/pdf";
        if (name.endsWith(".txt")) return "text/plain; charset=utf-8";
        if (name.endsWith(".zip")) return "application/zip";
        return "application/octet-stream";
    }
}

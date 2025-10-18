package com.file_exchange.dto;

import java.io.InputStream;

public class FileDto {
    private final String fileName;
    private final String contentType;
    private final InputStream inputStream;

    public FileDto(String fileName, String contentType, InputStream inputStream) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.inputStream = inputStream;
    }

    public String getFileName() { return fileName; }

    public String getContentType() { return contentType; }

    public InputStream getInputStream() { return inputStream; }
}

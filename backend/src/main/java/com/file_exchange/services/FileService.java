package com.file_exchange.services;

import com.file_exchange.repository.FileRepository;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class FileService {
    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public void uploadFile(Long userId, InputStream fileStream, String fileName, long size) {
        String filePath = "files/user" + userId + "/" + fileName;
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(filePath)) {
            fileStream.transferTo(fos);
            java.io.File file = new java.io.File(filePath);
            fileRepository.saveFile(userId, fileName, filePath, file.length());
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to upload file: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> getUserFiles(Long userId) {
        return fileRepository.getUserFiles(userId);
    }

    public InputStream getUserFile(Long userId, Long fileId) {
        return fileRepository.getUserFile(userId, fileId);
    }
}

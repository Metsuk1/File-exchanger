package com.file_exchange.services;

import com.file_exchange.repository.FileRepository;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import com.file_exchange.entity.File;

public class FileService {
    private final FileRepository fileRepository;
    private final String uploadsDir = "uploads";

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
        // create dir uploads if it doesn't exist
        new java.io.File(uploadsDir).mkdirs();
    }

    public Long uploadFile(Long userId, InputStream fileStream, String fileName, long size) {
        String userDir = uploadsDir + "/" + userId;
        new java.io.File(userDir).mkdirs(); // create dir for user

        String filePath = userDir + "/" + fileName;
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fileStream.transferTo(fos);
            File file = new File(null, userId, fileName, filePath, size);
            return fileRepository.saveFile(file); // return id
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to upload file: " + e.getMessage());
        }
    }

    public List<File> getUserFiles(Long userId) {
        return fileRepository.getUserFiles(userId);
    }

    public InputStream getUserFile(Long userId, Long fileId) {
        File file = fileRepository.getFileById(fileId, userId);
        if (file == null) {
            throw new IllegalArgumentException("File not found");
        }
        try {
            return new FileInputStream(file.getFilePath());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found", e);
        }

    }
}

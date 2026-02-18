package com.file_exchange.services;

import com.file_exchange.dto.FileDto;
import com.file_exchange.entity.File;
import com.file_exchange.entity.SharedLink;
import com.file_exchange.exceptions.NotFoundException;
import com.file_exchange.handlers.utilsFiles.MimeTypeUtils;
import com.file_exchange.repository.FileRepository;
import com.file_exchange.repository.SharedLinkRepository;
import com.file_exchange.storage.StorageService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class FileService {
    private final FileRepository fileRepository;
    private final SharedLinkRepository sharedLinkRepository;
    private final StorageService storageService;

    public FileService(
            FileRepository fileRepository, SharedLinkRepository sharedLinkRepository, StorageService storageService) {
        this.fileRepository = fileRepository;
        this.sharedLinkRepository = sharedLinkRepository;
        this.storageService = storageService;
    }

    public Long uploadFile(Long userId, InputStream fileStream, String fileName, long size) {
        String safeName = sanitizeFileName(fileName != null ? fileName : "unnamed");
        String objectKey = userId + "/" + UUID.randomUUID() + "_" + safeName;
        String contentType = MimeTypeUtils.detect(safeName);

        try (fileStream) {
            storageService.upload(objectKey, fileStream, size, contentType);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to upload file '" + fileName + "' for user " + userId + ": " + e.getMessage(), e);
        }

        File file = new File(null, userId, safeName, objectKey, size);
        return fileRepository.saveFile(file);
    }

    public List<File> getUserFiles(Long userId) {
        return fileRepository.getUserFiles(userId);
    }

    public FileDto getUserFile(Long userId, Long fileId) {
        File file = fileRepository.getFileById(fileId, userId);
        if (file == null) {
            throw new NotFoundException("File not found");
        }

        InputStream is = storageService.download(file.getFilePath());
        String contentType = MimeTypeUtils.detect(file.getFileName());
        return new FileDto(file.getFileName(), contentType, is);
    }

    public void deleteFile(Long userId, Long fileId) {
        File file = fileRepository.getFileById(fileId, userId);
        if (file == null) {
            throw new NotFoundException("File not found");
        }

        storageService.delete(file.getFilePath());
        sharedLinkRepository.deleteByFileId(fileId);
        fileRepository.deleteFile(fileId, userId);
    }

    public String createShareLink(Long userId, Long fileId) {
        File file = fileRepository.getFileById(fileId, userId);
        if (file == null) {
            throw new NotFoundException("File not found");
        }

        SharedLink existing = sharedLinkRepository.findByFileId(fileId);
        if (existing != null) {
            return existing.getToken();
        }

        String token = UUID.randomUUID().toString();
        SharedLink link = new SharedLink(null, fileId, token, Instant.now().toString());
        sharedLinkRepository.save(link);
        return token;
    }

    public FileDto getFileByShareToken(String token) {
        SharedLink link = sharedLinkRepository.findByToken(token);
        if (link == null) {
            throw new NotFoundException("Invalid share link");
        }

        File file = fileRepository.getFileById(link.getFileId());
        if (file == null) {
            throw new NotFoundException("File not found");
        }

        InputStream is = storageService.download(file.getFilePath());
        String contentType = MimeTypeUtils.detect(file.getFileName());
        return new FileDto(file.getFileName(), contentType, is);
    }

    public String getFilePath(Long userId, Long fileId) {
        File file = fileRepository.getFileById(fileId, userId);
        return file != null ? file.getFilePath() : null;
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null) return "unnamed";
        fileName = Paths.get(fileName).getFileName().toString();
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}

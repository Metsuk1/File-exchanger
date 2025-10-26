package com.file_exchange.controllers;

import com.file_exchange.annotations.*;
import com.file_exchange.dto.FileDto;
import com.file_exchange.entity.File;
import com.file_exchange.handlers.utilsFiles.TempFileInputStream;
import com.file_exchange.services.FileService;
import com.file_exchange.utils.JwtUtil;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@CustomRestController
@CustomRequestMapping("/api/v1/files")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @CustomPostMapping("/upload")
    public Map<String, Object> upload(@CustomRequestHeader("Authorization") String auth,
                                      @CustomRequestPart("file") TempFileInputStream filePart) {


        Long userId = extractUserId(auth);

        String fileName = filePart.getOriginalFileName();
        long size = filePart.getFileSize();

        Long fileId = fileService.uploadFile(userId, filePart, fileName, size);

        return Map.of("status", "ok", "fileId", fileId);
    }

    @CustomGetMapping
    public List<File> list(@CustomRequestHeader("Authorization") String auth) {
        Long userId = extractUserId(auth);

        return fileService.getUserFiles(userId);
    }

    @CustomGetMapping("/download")
    public FileDto download(@CustomRequestHeader("Authorization") String auth,
                            @CustomRequestParam("fileId") Long fileId) {

        Long userId = extractUserId(auth);

        return fileService.getUserFile(userId, fileId);
    }

    @CustomDeleteMapping
    public Map<String,Object> deleteFile(@CustomRequestHeader("Authorization") String auth,
                                         @CustomRequestParam("fileId") Long fileId){

        Long userId = extractUserId(auth);

        fileService.deleteFile(userId,fileId);

        return Map.of("status", "deleted", "fileId", fileId);
    }

    private Long extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7).trim();
        String userIdStr = JwtUtil.validateToken(token);

        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID in token");
        }
    }

}

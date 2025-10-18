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


        String token = auth.replace("Bearer ", "").trim();
        Long userId = Long.parseLong(JwtUtil.validateToken(token));

        String fileName = filePart.getOriginalFileName();
        long size = filePart.getFileSize();

        Long fileId = fileService.uploadFile(userId, filePart, fileName, size);

        return Map.of("status", "ok", "fileId", fileId);
    }

    @CustomGetMapping
    public List<File> list(@CustomRequestHeader("Authorization") String auth) {
        String token = auth.replace("Bearer ", "").trim();
        Long userId = Long.parseLong(JwtUtil.validateToken(token));

        return fileService.getUserFiles(userId);
    }

    @CustomGetMapping("/download")
    public FileDto download(@CustomRequestHeader("Authorization") String auth,
                            @CustomRequestParam("fileId") Long fileId) {

        String token = auth.replace("Bearer ", "").trim();

        Long userId = Long.parseLong(JwtUtil.validateToken(token));
        return fileService.getUserFile(userId, fileId);
    }
}

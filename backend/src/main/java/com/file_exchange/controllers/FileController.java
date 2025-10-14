package com.file_exchange.controllers;

import com.file_exchange.annotations.CustomGetMapping;
import com.file_exchange.annotations.CustomPostMapping;
import com.file_exchange.annotations.CustomRequestParam;
import com.file_exchange.services.FileService;
import com.file_exchange.utils.JwtUtil;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

//public class FileController {
//    private final FileService fileService;
//
//    public FileController(FileService fileService) {
//        this.fileService = fileService;
//    }
//
//    @CustomPostMapping("/upload")
//    public void upload(@CustomRequestHeader("Authorization") String auth,
//                       @CustomRequestPart("file") InputStream fileStream,
//                       @CustomRequestPart("fileName") String name,
//                       @CustomRequestPart("size") long size) {
//        Long userId = Long.parseLong(JwtUtil.validateToken(auth));
//        fileService.uploadFile(userId, fileStream, name, size);
//    }

//    @CustomGetMapping
//    public List<Map<String, Object>> list(@CustomRequestHeader("Authorization") String auth) {
//        Long userId = Long.parseLong(JwtUtil.validateToken(auth));
//        return fileService.getUserFiles(userId);
//    }
//
//    @CustomGetMapping("/download")
//    public InputStream download(@CustomRequestHeader("Authorization") String auth,
//                                @CustomRequestParam("fileId") Long fileId) {
//        Long userId = Long.parseLong(JwtUtil.validateToken(auth));
//        return fileService.getUserFile(userId, fileId);
//    }
//}

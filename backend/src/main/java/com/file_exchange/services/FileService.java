package com.file_exchange.services;

import com.file_exchange.dto.FileDto;
import com.file_exchange.handlers.utilsFiles.MimeTypeUtils;
import com.file_exchange.handlers.utilsFiles.TempFileInputStream;
import com.file_exchange.repository.FileRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

        String safeName = sanitizeFileName(fileName != null ? fileName : "unnamed");
        String filePath = userDir + "/" + safeName;
        Path targetPath = Paths.get(filePath);

        try {
            // if temp file — just move
            if (fileStream instanceof TempFileInputStream tempStream) {
                Path tempPath = tempStream.getTempFilePath();

                if (!Files.exists(tempPath)) {
                    throw new IllegalStateException("Temp file does not exist: " + tempPath);
                }

                Files.createDirectories(targetPath.getParent());

               try (tempStream) {
                   Files.copy(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
               }
            } else {
                try (fileStream; FileOutputStream fos = new FileOutputStream(filePath)) {
                    fileStream.transferTo(fos);
                }
            }

            // save metaData in DB
            File file = new File(null, userId, safeName, filePath, size);
            Long fileId = fileRepository.saveFile(file);

            return fileId;

        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to upload file '" + fileName + "' for user " + userId + ": " + e.getMessage(), e);
        }
    }

    public List<File> getUserFiles(Long userId) {
        return fileRepository.getUserFiles(userId);
    }

    public FileDto getUserFile(Long userId, Long fileId) {
        File file = fileRepository.getFileById(fileId, userId);
        if (file == null) {
            throw new IllegalArgumentException("File not found");
        }
        try {
            InputStream is = new FileInputStream(file.getFilePath());
            String contentType = null;

            try {
                contentType = Files.probeContentType(Paths.get(file.getFilePath()));
            }catch (Exception ignore) {
                // ignore,because there is no need to handle error and print it
            }

            if (contentType == null || contentType.isBlank()) {
                contentType = MimeTypeUtils.detect(file.getFileName());
            }

            return new FileDto(file.getFileName(),contentType,is);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found", e);
        }
    }

    public void deleteFile(Long userId, Long fileId) {
        File file = fileRepository.getFileById(fileId, userId);
        if(file == null) {
            throw new IllegalArgumentException("File not found");
        }

       Path path = Paths.get(file.getFilePath());

       try{
           if(Files.exists(path)){
              try{
                  Files.delete(path);
              }catch (java.nio.file.FileSystemException ex) {
                  try {
                      Thread.sleep(50);
                  } catch (InterruptedException ignored) {
                      // ignored for quick replay if the file was just used
                  }
                  Files.delete(path);
              }
           }
       } catch (IOException e) {
           throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
       }
        fileRepository.deleteFile(fileId,userId);
    }

    public String getFilePath(Long userId, Long fileId) {
        File file = fileRepository.getFileById(fileId, userId);
        return file != null ? file.getFilePath() : null;
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null) return "unnamed";
        // remove path only the name of file
        fileName = Paths.get(fileName).getFileName().toString();
        // replace danger symbols
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}

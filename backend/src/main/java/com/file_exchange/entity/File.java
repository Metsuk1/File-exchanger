package com.file_exchange.entity;

import java.time.LocalDateTime;

public class File {
    private Long id;
    private Long userId;
    private String fileName;
    private String filePath;
    private long size;

    public File() {}

    public File(Long id, Long userId, String fileName, String filePath, long size) {
        this.id = id;
        this.userId = userId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.size = size;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "File{" +
                "id=" + id +
                ", userId=" + userId +
                ", fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", size=" + size +
                '}';
    }
}

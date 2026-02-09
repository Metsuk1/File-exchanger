package com.file_exchange.entity;

public class SharedLink {
    private Long id;
    private Long fileId;
    private String token;
    private String createdAt;

    public SharedLink() {}

    public SharedLink(Long id, Long fileId, String token, String createdAt) {
        this.id = id;
        this.fileId = fileId;
        this.token = token;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

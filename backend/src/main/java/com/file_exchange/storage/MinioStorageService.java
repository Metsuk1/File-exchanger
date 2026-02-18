package com.file_exchange.storage;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinioStorageService implements StorageService {
    private static final Logger log = LoggerFactory.getLogger(MinioStorageService.class);

    private final MinioClient minioClient;
    private final String bucket;

    public MinioStorageService(MinioClient minioClient, String bucket) {
        this.minioClient = minioClient;
        this.bucket = bucket;
    }

    @Override
    public void upload(String objectKey, InputStream inputStream, long size, String contentType) {
        try {
            minioClient.putObject(PutObjectArgs.builder().bucket(bucket).object(objectKey).stream(inputStream, size, -1)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .build());
            log.info("Uploaded object: {}", objectKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload object to MinIO: " + objectKey, e);
        }
    }

    @Override
    public InputStream download(String objectKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucket).object(objectKey).build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to download object from MinIO: " + objectKey, e);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
            log.info("Deleted object: {}", objectKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete object from MinIO: " + objectKey, e);
        }
    }

    @Override
    public boolean exists(String objectKey) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucket).object(objectKey).build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

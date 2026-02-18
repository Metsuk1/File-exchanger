package com.file_exchange.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinioInitializer {
    private static final Logger log = LoggerFactory.getLogger(MinioInitializer.class);

    private final MinioClient minioClient;
    private final String bucket;

    public MinioInitializer(MinioClient minioClient, String bucket) {
        this.minioClient = minioClient;
        this.bucket = bucket;
    }

    public void initialize() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Created MinIO bucket: {}", bucket);
            } else {
                log.info("MinIO bucket already exists: {}", bucket);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MinIO bucket: " + bucket, e);
        }
    }
}

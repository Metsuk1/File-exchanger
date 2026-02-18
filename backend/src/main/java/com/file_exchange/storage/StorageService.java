package com.file_exchange.storage;

import java.io.InputStream;

public interface StorageService {
    void upload(String objectKey, InputStream inputStream, long size, String contentType);

    InputStream download(String objectKey);

    void delete(String objectKey);

    boolean exists(String objectKey);
}

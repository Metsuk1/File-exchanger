package com.file_exchange.unit.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.file_exchange.storage.MinioStorageService;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("MinioStorageService tests")
public class MinioStorageServiceTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private GetObjectResponse getObjectResponse;

    @Mock
    private StatObjectResponse statObjectResponse;

    private MinioStorageService storageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        storageService = new MinioStorageService(minioClient, "test-bucket");
    }

    @Test
    @DisplayName("Should upload object successfully")
    void testUpload() throws Exception {
        InputStream stream = new ByteArrayInputStream("test data".getBytes());

        storageService.upload("1/test.txt", stream, 9L, "text/plain");

        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException on upload failure")
    void testUploadFailure() throws Exception {
        InputStream stream = new ByteArrayInputStream("test data".getBytes());
        when(minioClient.putObject(any(PutObjectArgs.class))).thenThrow(new RuntimeException("Connection refused"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            storageService.upload("1/test.txt", stream, 9L, "text/plain");
        });
        assertTrue(ex.getMessage().contains("Failed to upload object to MinIO"));
    }

    @Test
    @DisplayName("Should download object successfully")
    void testDownload() throws Exception {
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(getObjectResponse);

        InputStream result = storageService.download("1/test.txt");

        assertNotNull(result);
        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException on download failure")
    void testDownloadFailure() throws Exception {
        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(new RuntimeException("Not found"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            storageService.download("1/missing.txt");
        });
        assertTrue(ex.getMessage().contains("Failed to download object from MinIO"));
    }

    @Test
    @DisplayName("Should delete object successfully")
    void testDelete() throws Exception {
        storageService.delete("1/test.txt");

        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException on delete failure")
    void testDeleteFailure() throws Exception {
        doThrow(new RuntimeException("Connection refused")).when(minioClient).removeObject(any(RemoveObjectArgs.class));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            storageService.delete("1/test.txt");
        });
        assertTrue(ex.getMessage().contains("Failed to delete object from MinIO"));
    }

    @Test
    @DisplayName("Should return true when object exists")
    void testExistsTrue() throws Exception {
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(statObjectResponse);

        assertTrue(storageService.exists("1/test.txt"));
        verify(minioClient).statObject(any(StatObjectArgs.class));
    }

    @Test
    @DisplayName("Should return false when object does not exist")
    void testExistsFalse() throws Exception {
        when(minioClient.statObject(any(StatObjectArgs.class))).thenThrow(new RuntimeException("Not found"));

        assertFalse(storageService.exists("1/missing.txt"));
    }

    @Test
    @DisplayName("Should use default content type when null is provided")
    void testUploadWithNullContentType() throws Exception {
        InputStream stream = new ByteArrayInputStream("data".getBytes());

        storageService.upload("1/file.bin", stream, 4L, null);

        verify(minioClient).putObject(any(PutObjectArgs.class));
    }
}

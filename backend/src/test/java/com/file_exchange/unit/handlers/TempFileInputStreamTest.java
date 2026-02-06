package com.file_exchange.unit.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import com.file_exchange.handlers.utilsFiles.TempFileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("TempFileInputStream tests")
class TempFileInputStreamTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should create temp file input stream")
    void testCreateStream() throws IOException {
        Path tempFile = Files.createTempFile(tempDir, "test", ".tmp");
        Files.writeString(tempFile, "test content");

        try (TempFileInputStream stream = new TempFileInputStream(tempFile, "original.txt", 12L)) {
            assertThat(stream.getTempFilePath()).isEqualTo(tempFile);
            assertThat(stream.getOriginalFileName()).isEqualTo("original.txt");
            assertThat(stream.getFileSize()).isEqualTo(12L);
        }
    }

    @Test
    @DisplayName("Should read content from temp file")
    void testReadContent() throws IOException {
        Path tempFile = Files.createTempFile(tempDir, "test", ".tmp");
        Files.writeString(tempFile, "hello world");

        try (TempFileInputStream stream = new TempFileInputStream(tempFile, "test.txt", 11L)) {
            byte[] content = stream.readAllBytes();
            assertThat(new String(content)).isEqualTo("hello world");
        }
    }

    @Test
    @DisplayName("Should delete temp file on close")
    void testDeleteOnClose() throws IOException {
        Path tempFile = Files.createTempFile(tempDir, "test", ".tmp");
        Files.writeString(tempFile, "to be deleted");

        TempFileInputStream stream = new TempFileInputStream(tempFile, "delete.txt", 13L);
        assertThat(Files.exists(tempFile)).isTrue();

        stream.close();

        assertThat(Files.exists(tempFile)).isFalse();
    }

    @Test
    @DisplayName("Should handle multiple close calls")
    void testMultipleClose() throws IOException {
        Path tempFile = Files.createTempFile(tempDir, "test", ".tmp");
        Files.writeString(tempFile, "content");

        TempFileInputStream stream = new TempFileInputStream(tempFile, "multi.txt", 7L);
        stream.close();
        stream.close(); // Second close should not throw

        assertThat(Files.exists(tempFile)).isFalse();
    }
}

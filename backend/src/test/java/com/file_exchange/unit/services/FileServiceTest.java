package com.file_exchange.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

import com.file_exchange.entity.File;
import com.file_exchange.repository.FileRepository;
import com.file_exchange.services.FileService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("FileService tests")
public class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    private FileService fileService;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        fileService = new FileService(fileRepository);
        tempDir = Files.createTempDirectory("test-uploads");
    }

    @AfterEach
    void tearDown() throws IOException {
        // cleanup temp files
        Files.walk(tempDir).sorted((a, b) -> -a.compareTo(b)).forEach(path -> {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                // ignoring because cleaning is not critical
            }
        });
    }

    @Test
    @DisplayName("Should upload files successfully")
    void testUploadFile() {
        Long userId = 1L;
        String fileName = "test.txt";
        InputStream stream = new ByteArrayInputStream("content".getBytes());
        long size = 7L;

        when(fileRepository.saveFile(any(File.class))).thenReturn(100L);

        Long fileId = fileService.uploadFile(userId, stream, fileName, size);

        assertNotNull(fileId);
        assertEquals(100L, fileId);
        verify(fileRepository, times(1)).saveFile(any(File.class));
    }

    @Test
    @DisplayName("Should sanitize dangerous file names")
    void testSanitizeFileName() {
        Long userId = 1L;
        String dangerousName = "../../../etc/passwd";
        InputStream stream = new ByteArrayInputStream("content".getBytes());

        when(fileRepository.saveFile(any(File.class))).thenReturn(100L);

        assertDoesNotThrow(() -> {
            fileService.uploadFile(userId, stream, dangerousName, 7L);
        });
    }

    @Test
    @DisplayName("Should get user files")
    void testGetUserFiles() {
        Long userId = 1L;
        List<File> expectedFiles = List.of(
                new File(1L, userId, "file1.txt", "/path/file1.txt", 100L),
                new File(2L, userId, "file2.pdf", "/path/file2.pdf", 200L));

        when(fileRepository.getUserFiles(userId)).thenReturn(expectedFiles);

        List<File> files = fileService.getUserFiles(userId);

        assertEquals(2, files.size());
        assertEquals("file1.txt", files.get(0).getFileName());
        verify(fileRepository, times(1)).getUserFiles(userId);
    }

    @Test
    @DisplayName("Should throw exception for non-existent file")
    void testGetNonExistentFile() {
        Long userId = 1L;
        Long fileId = 999L;

        when(fileRepository.getFileById(fileId, userId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> {
            fileService.getUserFile(userId, fileId);
        });
    }

    @Test
    @DisplayName("Should delete file successfully")
    void testDeleteFile() throws IOException {
        Long userId = 1L;
        Long fileId = 1L;

        // Create a real temp file to delete
        Path testFile = Files.createTempFile(tempDir, "delete-test", ".txt");
        Files.writeString(testFile, "content to delete");

        File file = new File(fileId, userId, "delete-test.txt", testFile.toString(), 17L);
        when(fileRepository.getFileById(fileId, userId)).thenReturn(file);
        doNothing().when(fileRepository).deleteFile(fileId, userId);

        assertDoesNotThrow(() -> fileService.deleteFile(userId, fileId));
        verify(fileRepository, times(1)).deleteFile(fileId, userId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent file")
    void testDeleteNonExistentFile() {
        Long userId = 1L;
        Long fileId = 999L;

        when(fileRepository.getFileById(fileId, userId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> {
            fileService.deleteFile(userId, fileId);
        });
    }

    @Test
    @DisplayName("Should get file path")
    void testGetFilePath() {
        Long userId = 1L;
        Long fileId = 1L;
        String expectedPath = "/path/to/file.txt";

        File file = new File(fileId, userId, "file.txt", expectedPath, 100L);
        when(fileRepository.getFileById(fileId, userId)).thenReturn(file);

        String path = fileService.getFilePath(userId, fileId);

        assertEquals(expectedPath, path);
    }

    @Test
    @DisplayName("Should return null for non-existent file path")
    void testGetFilePathNotFound() {
        Long userId = 1L;
        Long fileId = 999L;

        when(fileRepository.getFileById(fileId, userId)).thenReturn(null);

        String path = fileService.getFilePath(userId, fileId);

        assertNull(path);
    }

    @Test
    @DisplayName("Should handle null filename in upload")
    void testUploadFileWithNullFilename() {
        Long userId = 1L;
        InputStream stream = new ByteArrayInputStream("content".getBytes());

        when(fileRepository.saveFile(any(File.class))).thenReturn(100L);

        Long fileId = fileService.uploadFile(userId, stream, null, 7L);

        assertNotNull(fileId);
        assertEquals(100L, fileId);
    }

    @Test
    @DisplayName("Should handle empty user files list")
    void testGetUserFilesEmpty() {
        Long userId = 1L;

        when(fileRepository.getUserFiles(userId)).thenReturn(List.of());

        List<File> files = fileService.getUserFiles(userId);

        assertTrue(files.isEmpty());
        verify(fileRepository, times(1)).getUserFiles(userId);
    }
}

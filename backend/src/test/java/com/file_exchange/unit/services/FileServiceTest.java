package com.file_exchange.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.file_exchange.dto.FileDto;
import com.file_exchange.entity.File;
import com.file_exchange.entity.SharedLink;
import com.file_exchange.exceptions.NotFoundException;
import com.file_exchange.repository.FileRepository;
import com.file_exchange.repository.SharedLinkRepository;
import com.file_exchange.services.FileService;
import com.file_exchange.storage.StorageService;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("FileService tests")
public class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private SharedLinkRepository sharedLinkRepository;

    @Mock
    private StorageService storageService;

    private FileService fileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fileService = new FileService(fileRepository, sharedLinkRepository, storageService);
    }

    @Test
    @DisplayName("Should upload files successfully")
    void testUploadFile() {
        Long userId = 1L;
        String fileName = "test.txt";
        InputStream stream = new ByteArrayInputStream("content".getBytes());
        long size = 7L;

        doNothing().when(storageService).upload(anyString(), any(InputStream.class), anyLong(), anyString());
        when(fileRepository.saveFile(any(File.class))).thenReturn(100L);

        Long fileId = fileService.uploadFile(userId, stream, fileName, size);

        assertNotNull(fileId);
        assertEquals(100L, fileId);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(storageService).upload(keyCaptor.capture(), any(InputStream.class), eq(7L), anyString());
        String objectKey = keyCaptor.getValue();
        assertTrue(objectKey.startsWith("1/"), "Object key should start with userId");
        assertTrue(objectKey.endsWith("_test.txt"), "Object key should end with sanitized filename");
        verify(fileRepository).saveFile(any(File.class));
    }

    @Test
    @DisplayName("Should sanitize dangerous file names and strip path traversal")
    void testSanitizeFileName() {
        Long userId = 1L;
        String dangerousName = "../../../etc/passwd";
        InputStream stream = new ByteArrayInputStream("content".getBytes());

        doNothing().when(storageService).upload(anyString(), any(InputStream.class), anyLong(), anyString());
        when(fileRepository.saveFile(any(File.class))).thenReturn(100L);

        fileService.uploadFile(userId, stream, dangerousName, 7L);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(storageService).upload(keyCaptor.capture(), any(InputStream.class), anyLong(), anyString());
        String objectKey = keyCaptor.getValue();
        assertFalse(objectKey.contains(".."), "Object key must not contain path traversal");
        assertFalse(objectKey.contains("/etc/"), "Object key must not contain traversed path");

        ArgumentCaptor<File> fileCaptor = ArgumentCaptor.forClass(File.class);
        verify(fileRepository).saveFile(fileCaptor.capture());
        String savedFileName = fileCaptor.getValue().getFileName();
        assertEquals("passwd", savedFileName, "Only the base filename should be stored");
    }

    @Test
    @DisplayName("Should get user files")
    void testGetUserFiles() {
        Long userId = 1L;
        List<File> expectedFiles = List.of(
                new File(1L, userId, "file1.txt", "1/abc_file1.txt", 100L),
                new File(2L, userId, "file2.pdf", "1/def_file2.pdf", 200L));

        when(fileRepository.getUserFiles(userId)).thenReturn(expectedFiles);

        List<File> files = fileService.getUserFiles(userId);

        assertEquals(2, files.size());
        assertEquals("file1.txt", files.get(0).getFileName());
        verify(fileRepository).getUserFiles(userId);
    }

    @Test
    @DisplayName("Should throw exception for non-existent file")
    void testGetNonExistentFile() {
        Long userId = 1L;
        Long fileId = 999L;

        when(fileRepository.getFileById(fileId, userId)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> {
            fileService.getUserFile(userId, fileId);
        });
    }

    @Test
    @DisplayName("Should delete file successfully")
    void testDeleteFile() {
        Long userId = 1L;
        Long fileId = 1L;

        File file = new File(fileId, userId, "delete-test.txt", "1/uuid_delete-test.txt", 17L);
        when(fileRepository.getFileById(fileId, userId)).thenReturn(file);
        doNothing().when(storageService).delete("1/uuid_delete-test.txt");
        doNothing().when(fileRepository).deleteFile(fileId, userId);

        assertDoesNotThrow(() -> fileService.deleteFile(userId, fileId));
        verify(storageService).delete("1/uuid_delete-test.txt");
        verify(fileRepository).deleteFile(fileId, userId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent file")
    void testDeleteNonExistentFile() {
        Long userId = 1L;
        Long fileId = 999L;

        when(fileRepository.getFileById(fileId, userId)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> {
            fileService.deleteFile(userId, fileId);
        });
    }

    @Test
    @DisplayName("Should get file path")
    void testGetFilePath() {
        Long userId = 1L;
        Long fileId = 1L;
        String expectedPath = "1/uuid_file.txt";

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

        doNothing().when(storageService).upload(anyString(), any(InputStream.class), anyLong(), anyString());
        when(fileRepository.saveFile(any(File.class))).thenReturn(100L);

        Long fileId = fileService.uploadFile(userId, stream, null, 7L);

        assertNotNull(fileId);
        assertEquals(100L, fileId);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(storageService).upload(keyCaptor.capture(), any(InputStream.class), anyLong(), anyString());
        assertTrue(keyCaptor.getValue().endsWith("_unnamed"), "Null filename should default to 'unnamed'");
    }

    @Test
    @DisplayName("Should handle empty user files list")
    void testGetUserFilesEmpty() {
        Long userId = 1L;

        when(fileRepository.getUserFiles(userId)).thenReturn(List.of());

        List<File> files = fileService.getUserFiles(userId);

        assertTrue(files.isEmpty());
        verify(fileRepository).getUserFiles(userId);
    }

    // ==================== getUserFile (happy path) ====================

    @Test
    @DisplayName("Should get user file successfully")
    void testGetUserFileSuccess() {
        Long userId = 1L;
        Long fileId = 1L;

        File file = new File(fileId, userId, "download-test.txt", "1/uuid_download-test.txt", 12L);
        when(fileRepository.getFileById(fileId, userId)).thenReturn(file);

        InputStream mockStream = new ByteArrayInputStream("file content".getBytes());
        when(storageService.download("1/uuid_download-test.txt")).thenReturn(mockStream);

        FileDto dto = fileService.getUserFile(userId, fileId);

        assertNotNull(dto);
        assertEquals("download-test.txt", dto.getFileName());
        assertNotNull(dto.getContentType());
        assertNotNull(dto.getInputStream());
        verify(storageService).download("1/uuid_download-test.txt");
    }

    // ==================== createShareLink ====================

    @Test
    @DisplayName("Should create new share link")
    void testCreateShareLinkNew() {
        Long userId = 1L;
        Long fileId = 1L;

        File file = new File(fileId, userId, "file.txt", "1/uuid_file.txt", 100L);
        when(fileRepository.getFileById(fileId, userId)).thenReturn(file);
        when(sharedLinkRepository.findByFileId(fileId)).thenReturn(null);

        String token = fileService.createShareLink(userId, fileId);

        assertNotNull(token);
        assertFalse(token.isBlank());
        verify(sharedLinkRepository).save(any(SharedLink.class));
    }

    @Test
    @DisplayName("Should return existing share link")
    void testCreateShareLinkExisting() {
        Long userId = 1L;
        Long fileId = 1L;

        File file = new File(fileId, userId, "file.txt", "1/uuid_file.txt", 100L);
        SharedLink existing = new SharedLink(1L, fileId, "existing-token", "2024-01-01");

        when(fileRepository.getFileById(fileId, userId)).thenReturn(file);
        when(sharedLinkRepository.findByFileId(fileId)).thenReturn(existing);

        String token = fileService.createShareLink(userId, fileId);

        assertEquals("existing-token", token);
        verify(sharedLinkRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when creating share link for non-existent file")
    void testCreateShareLinkFileNotFound() {
        when(fileRepository.getFileById(999L, 1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> fileService.createShareLink(1L, 999L));
    }

    // ==================== getFileByShareToken ====================

    @Test
    @DisplayName("Should get file by share token successfully")
    void testGetFileByShareTokenSuccess() {
        SharedLink link = new SharedLink(1L, 1L, "valid-token", "2024-01-01");
        File file = new File(1L, 1L, "shared-test.txt", "1/uuid_shared-test.txt", 14L);

        when(sharedLinkRepository.findByToken("valid-token")).thenReturn(link);
        when(fileRepository.getFileById(1L)).thenReturn(file);

        InputStream mockStream = new ByteArrayInputStream("shared content".getBytes());
        when(storageService.download("1/uuid_shared-test.txt")).thenReturn(mockStream);

        FileDto dto = fileService.getFileByShareToken("valid-token");

        assertNotNull(dto);
        assertEquals("shared-test.txt", dto.getFileName());
        assertNotNull(dto.getInputStream());
        verify(storageService).download("1/uuid_shared-test.txt");
    }

    @Test
    @DisplayName("Should throw exception for invalid share token")
    void testGetFileByShareTokenInvalid() {
        when(sharedLinkRepository.findByToken("bad-token")).thenReturn(null);

        NotFoundException ex =
                assertThrows(NotFoundException.class, () -> fileService.getFileByShareToken("bad-token"));
        assertEquals("Invalid share link", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when shared file not found in DB")
    void testGetFileByShareTokenFileNotFound() {
        SharedLink link = new SharedLink(1L, 999L, "token", "2024-01-01");
        when(sharedLinkRepository.findByToken("token")).thenReturn(link);
        when(fileRepository.getFileById(999L)).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> fileService.getFileByShareToken("token"));
        assertEquals("File not found", ex.getMessage());
    }

    // ==================== deleteFile (storage delete) ====================

    @Test
    @DisplayName("Should delete file from storage and DB")
    void testDeleteFileFromStorage() {
        Long userId = 1L;
        Long fileId = 1L;

        File file = new File(fileId, userId, "gone.txt", "1/uuid_gone.txt", 10L);
        when(fileRepository.getFileById(fileId, userId)).thenReturn(file);
        doNothing().when(storageService).delete("1/uuid_gone.txt");

        assertDoesNotThrow(() -> fileService.deleteFile(userId, fileId));
        verify(storageService).delete("1/uuid_gone.txt");
        verify(sharedLinkRepository).deleteByFileId(fileId);
        verify(fileRepository).deleteFile(fileId, userId);
    }

    // ==================== upload edge cases ====================

    @Test
    @DisplayName("Should generate unique object keys for same filename")
    void testUploadSameFileNameGeneratesUniqueKeys() {
        Long userId = 1L;
        String fileName = "report.pdf";

        doNothing().when(storageService).upload(anyString(), any(InputStream.class), anyLong(), anyString());
        when(fileRepository.saveFile(any(File.class))).thenReturn(1L).thenReturn(2L);

        InputStream stream1 = new ByteArrayInputStream("v1".getBytes());
        InputStream stream2 = new ByteArrayInputStream("v2".getBytes());

        fileService.uploadFile(userId, stream1, fileName, 2L);
        fileService.uploadFile(userId, stream2, fileName, 2L);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(storageService, times(2)).upload(keyCaptor.capture(), any(InputStream.class), anyLong(), anyString());

        List<String> keys = keyCaptor.getAllValues();
        assertNotEquals(keys.get(0), keys.get(1), "Each upload should have a unique object key");
    }
}

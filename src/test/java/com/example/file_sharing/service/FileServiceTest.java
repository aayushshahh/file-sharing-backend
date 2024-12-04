package com.example.file_sharing.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;

import javax.crypto.SecretKey;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.file_sharing.data.FileMetadata;
import com.example.file_sharing.repository.FileMetadataRepository;

public class FileServiceTest {
    @Mock
    private FileMetadataRepository repository;

    @InjectMocks
    private FileService fileService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        new File("storage").mkdirs();
    }

    @Test
    void testUploadFile() throws Exception {
        byte[] fileData = "Test Data".getBytes();
        String fileName = "test.txt";
        String password = "password";
        String contentType = "text/plain";

        FileMetadata savedMetadata = new FileMetadata(
                100015L, fileName, "storage/unique-test.txt", "unique-url", LocalDateTime.now(), contentType);

        when(repository.save(any(FileMetadata.class))).thenReturn(savedMetadata);

        String uniqueURL = fileService.uploadFile(fileData, fileName, password, contentType);

        assertNotNull(uniqueURL);
        verify(repository, times(1)).save(any(FileMetadata.class));
    }

    @Test
    void testDownloadFile() throws Exception {
        // Arrange
        String uniqueURL = "unique-url";
        String password = "password";
        byte[] fileData = "Test Data".getBytes();

        // Simulating encryption in the uploadFile method
        SecretKey secretKey = fileService.generateKey(password);
        byte[] encryptedFile = fileService.encryptFile(fileData, secretKey);

        // Setting up metadata to simulate a real file in the repository
        FileMetadata metadata = new FileMetadata(
                1L, "test.txt", "storage/test.txt", uniqueURL, LocalDateTime.now(), "text/plain");

        // Mocking the repository to return the file metadata
        when(repository.findByUniqueURL(uniqueURL)).thenReturn(metadata);

        // Writing the encrypted file to storage (simulating file storage)
        FileUtils.writeByteArrayToFile(new File(metadata.getFileLocation()), encryptedFile);

        // Act
        File decryptedFile = fileService.downloadFile(uniqueURL, password);

        // Assert
        assertNotNull(decryptedFile);
        assertTrue(decryptedFile.exists());
        verify(repository, times(1)).findByUniqueURL(uniqueURL);
    }

    @Test
    void testCleanExpiredFiles() {
        FileMetadata metadata = new FileMetadata(
                100015L, "expired.txt", "storage/expired.txt", "expired-url", LocalDateTime.now().minusDays(3),
                "text/plain");

        when(repository.findByUploadTimeStampBefore(any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(metadata));

        fileService.cleanExpiredFiles();

        verify(repository, times(1)).findByUploadTimeStampBefore(any(LocalDateTime.class));
        verify(repository, times(1)).delete(metadata);
    }

    @Test
    void testGenerateKey() throws Exception {
        String password = "password";

        SecretKey key = fileService.generateKey(password);

        assertNotNull(key);
    }

    @Test
    void testEncryptAndDecryptFile() throws Exception {
        String password = "password";
        byte[] fileData = "Test Data".getBytes();

        SecretKey key = fileService.generateKey(password);

        byte[] encryptedData = fileService.encryptFile(fileData, key);
        byte[] decryptedData = fileService.decryptFile(encryptedData, key);

        assertArrayEquals(fileData, decryptedData);
    }
}

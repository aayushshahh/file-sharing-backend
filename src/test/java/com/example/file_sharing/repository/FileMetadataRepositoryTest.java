package com.example.file_sharing.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.example.file_sharing.data.FileMetadata;

@DataJpaTest
public class FileMetadataRepositoryTest {
    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    private FileMetadata file1;
    private FileMetadata file2;

    @BeforeEach
    void setup() {
        LocalDateTime now = LocalDateTime.now();

        file1 = new FileMetadata(null, "file1.txt", "/files/file1.txt", "url1", now.minusHours(2), "text/plain");
        file2 = new FileMetadata(null, "file2.txt", "/files/file2.txt", "url2", now.minusHours(5), "text/plain");

        // Save initial records
        fileMetadataRepository.save(file1);
        fileMetadataRepository.save(file2);
    }

    @Test
    void testFindByUploadTimeStampBefore() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(3);

        List<FileMetadata> results = fileMetadataRepository.findByUploadTimeStampBefore(cutoffDate);

        assertEquals(1, results.size());
        assertEquals("file2.txt", results.get(0).getFileName());
    }

    @Test
    void testFindByUniqueURL() {
        FileMetadata result = fileMetadataRepository.findByUniqueURL("url1");

        assertNotNull(result);
        assertEquals("file1.txt", result.getFileName());
        assertEquals("/files/file1.txt", result.getFileLocation());
    }
}

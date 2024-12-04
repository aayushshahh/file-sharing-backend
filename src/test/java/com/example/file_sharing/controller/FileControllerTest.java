package com.example.file_sharing.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.example.file_sharing.data.FileMetadata;
import com.example.file_sharing.service.FileService;

@WebMvcTest(FileController.class)
public class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new FileController(fileService)).build();
    }

    @Test
    void testUploadFile() throws Exception {
        String password = "password";
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "Hello, World!".getBytes());
        String expectedUrl = "unique-url";

        when(fileService.uploadFile(any(byte[].class), eq("test.txt"), eq(password), eq("text/plain")))
                .thenReturn(expectedUrl);

        mockMvc.perform(multipart("/api/v1/files/upload")
                .file(file)
                .param("password", password))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("http://localhost/api/v1/files/download/unique-url"));
    }

    @Test
    void testDownloadFile() throws Exception {
        String uniqueURL = "unique-url";
        String password = "password";
        FileMetadata fileMetadata = new FileMetadata();
        String filePath = "storage/" + uniqueURL + "-test.txt";
        fileMetadata.setUniqueURL(uniqueURL);
        fileMetadata.setFileName("test.txt");
        fileMetadata.setFileLocation(filePath);
        fileMetadata.setContentType("text/plain");

        File mockFile = mock(File.class);
        when(mockFile.exists()).thenReturn(true);
        when(mockFile.length()).thenReturn(13L);
        when(mockFile.getPath()).thenReturn(filePath);

        byte[] fileContent = "Hello, World!".getBytes();
        try (MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.readAllBytes(Paths.get(filePath)))
                    .thenReturn(fileContent);

            when(fileService.downloadFile(eq(uniqueURL), eq(password))).thenReturn(mockFile);
            when(fileService.getMetadata(eq(uniqueURL))).thenReturn(fileMetadata);

            mockMvc.perform(get("/api/v1/files/download/{url}", uniqueURL)
                    .param("password", password))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test.txt\""))
                    .andExpect(content().contentType(MediaType.parseMediaType("text/plain")));
        }
    }
}

package com.example.file_sharing.controller;

import java.io.File;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.file_sharing.data.FileMetadata;
import com.example.file_sharing.exception.MissingPasswordException;
import com.example.file_sharing.service.FileService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {
    private final FileService fileService;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
            @RequestParam("password") String password, HttpServletRequest request) {
        try {
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("File size exceeds the limit of 1MB");
            }
            if (password.equals("") || password.length() == 0) {
                throw new MissingPasswordException("Password is required to store the files");
            }
            String testEndpointString = fileService.uploadFile(file.getBytes(), file.getOriginalFilename(), password,
                    file.getContentType());
            String serverURL = request.getScheme() + "://" + request.getServerName() + (request.getServerPort() == 80
                    || request.getServerPort() == 443 ? "" : ":" + request.getServerPort());
            String completeDownloadURL = serverURL + "/api/v1/files/download/" + testEndpointString;
            return ResponseEntity.ok(completeDownloadURL);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/download/{url}")
    public ResponseEntity<?> downloadFile(@PathVariable("url") String url, @RequestParam("password") String password) {
        try {
            if (password.equals("") || password.length() == 0) {
                throw new MissingPasswordException("Password is required to decrypt and access the files");
            }
            File originalFile = fileService.downloadFile(url, password);
            FileMetadata metadata = fileService.getMetadata(url);
            Resource resource = new FileSystemResource(originalFile);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + metadata.getFileName() + "\"").contentType(
                            MediaType.parseMediaType(metadata.getContentType()))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}

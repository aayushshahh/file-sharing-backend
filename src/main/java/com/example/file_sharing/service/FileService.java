package com.example.file_sharing.service;

import java.io.File;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;
import com.example.file_sharing.repository.FileMetadataRepository;

@Service
public class FileService {
    private final FileMetadataRepository repository;
    private static final String STORAGE_DIRECTORY = "storage/";

    public FileService(FileMetadataRepository repository) {
        this.repository = repository;
        new File(STORAGE_DIRECTORY).mkdirs();
    }

    public String uploadFile(byte[] fileData, String fileName, String password) throws Exception {
        SecretKey secretKey = generateKey(password);
        // ToDo Implementation for File upload
        return "This is successfull connection, encrypted passcode key is " + secretKey.getEncoded();
    }

    private SecretKey generateKey(String password) {
        byte[] key = Base64.getDecoder().decode(password);
        return new SecretKeySpec(key, 0, key.length, "AES");
    }
}

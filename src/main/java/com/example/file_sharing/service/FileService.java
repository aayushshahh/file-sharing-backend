package com.example.file_sharing.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.file_sharing.data.FileMetadata;
import com.example.file_sharing.exception.IncorrectPasswordException;
import com.example.file_sharing.repository.FileMetadataRepository;

@Service
public class FileService {
    private final FileMetadataRepository repository;
    private static final String STORAGE_DIRECTORY = "storage/";

    public FileService(FileMetadataRepository repository) {
        this.repository = repository;
        new File(STORAGE_DIRECTORY).mkdirs();
    }

    public String uploadFile(byte[] fileData, String fileName, String password, String contentType) throws Exception {
        SecretKey secretKey = generateKey(password);
        byte[] encryptedFile = encryptFile(fileData, secretKey);

        String uniqueFileName = UUID.randomUUID() + "-" + fileName;
        String filePath = STORAGE_DIRECTORY + uniqueFileName;
        FileUtils.writeByteArrayToFile(new File(filePath), encryptedFile);

        FileMetadata metadata = new FileMetadata();
        metadata.setFileName(fileName);
        metadata.setFileLocation(filePath);
        metadata.setUploadTimeStamp(LocalDateTime.now());
        metadata.setUniqueURL(UUID.randomUUID().toString());
        metadata.setContentType(contentType);
        repository.save(metadata);
        return metadata.getUniqueURL();
    }

    public File downloadFile(String uniqueURL, String password) throws Exception {
        FileMetadata metadata = repository.findByUniqueURL(uniqueURL);
        if (metadata == null) {
            throw new FileNotFoundException(
                    "The File associated with the URL is not found, its either missing or expired");
        }
        byte[] encryptedData = Files.readAllBytes(Paths.get(metadata.getFileLocation()));
        SecretKey secretKey = generateKey(password);
        try {
            byte[] decryptedData = decryptFile(encryptedData, secretKey);
            File tempFile = File.createTempFile("decrypted-", metadata.getFileName());
            Files.write(tempFile.toPath(), decryptedData);
            return tempFile;
        } catch (Exception e) {
            throw new IncorrectPasswordException("The password provided is incorrect, cannot access secure file");
        }
    }

    @Scheduled(fixedRate = 3600000) // milliseconds
    public void cleanExpiredFiles() {
        LocalDateTime expiryTime = LocalDateTime.now().minusHours(48);
        repository.findByUploadTimeStampBefore(expiryTime).forEach(metadata -> {
            new File(metadata.getFileLocation()).delete();
            repository.delete(metadata);
        });
        System.out.println("Expired Files Cleaned Up at " + Instant.now());
    }

    public SecretKey generateKey(String password) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(password.getBytes("UTF-8"));
        return new SecretKeySpec(key, 0, 32, "AES");
    }

    public byte[] encryptFile(byte[] data, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    public byte[] decryptFile(byte[] data, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    public FileMetadata getMetadata(String uniqueURL) {
        return repository.findByUniqueURL(uniqueURL);
    }
}

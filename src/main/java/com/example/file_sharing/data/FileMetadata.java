package com.example.file_sharing.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity // Map class to database table
@Data // Lombok Annotation for Getters and Setters
@NoArgsConstructor
@AllArgsConstructor 
public class FileMetadata {
    
    @Id // Primary Key for Database
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Generate Unique ID
    private Long id;

    private String fileName;
    private String fileLocation;
    private String uniqueURL;
    private LocalDateTime uploadTimeStamp;
}

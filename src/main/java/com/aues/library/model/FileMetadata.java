package com.aues.library.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String publicId;
    private String resourceType;
    private String url;

    @ManyToOne
    @JoinColumn(name = "book_copy_id")
    private BookCopy bookCopy;

    // Getters and setters
}

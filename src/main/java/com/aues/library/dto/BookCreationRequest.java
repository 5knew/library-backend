package com.aues.library.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Data
public class BookCreationRequest {
    private MultipartFile fullPdfFile;
    private MultipartFile previewPdfFile;
    private MultipartFile imageFile;
    private String name;
    private List<Long> authorIds;
    private String description;
    private Date publicationDate;
    private List<Long> categoryIds;
}


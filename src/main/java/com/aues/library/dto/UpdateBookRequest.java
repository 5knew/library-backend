package com.aues.library.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Data
public class UpdateBookRequest {
    private MultipartFile fullPdfFile;
    private MultipartFile previewPdfFile;
    private MultipartFile imageFile;
    private String name;
    private String description;
    private List<Long> authorIds;
    private List<Long> categoryIds;
}

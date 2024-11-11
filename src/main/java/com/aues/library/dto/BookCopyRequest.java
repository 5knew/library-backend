package com.aues.library.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class BookCopyRequest {
    private Long bookId; // ID of the book
    private BigDecimal price;
    private Date publicationDate;
    private String language;
    private MultipartFile fullPdf;


}


package com.aues.library.service.impl;

import com.aues.library.controller.BookController;
import com.aues.library.repository.BookCopyRepository;
import com.aues.library.repository.BookRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;

    public CloudinaryService(@Value("${cloudinary.cloud_name}") String cloudName,
                             @Value("${cloudinary.api_key}") String apiKey,
                             @Value("${cloudinary.api_secret}") String apiSecret,
                             BookRepository bookRepository,
                             BookCopyRepository bookCopyRepository
                             ) {
        this.bookRepository = bookRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }


    // Upload a file for a book and return its URL
    public String uploadFileForBook(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            logger.warn("No file provided for upload");
            return null;
        }
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String fileUrl = uploadResult.get("url").toString();
            logger.info("File uploaded successfully to Cloudinary: {}", fileUrl);
            return fileUrl;
        } catch (IOException e) {
            logger.error("Failed to upload file to Cloudinary", e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    // Delete a file for a book by its URL
    public void deleteFileForBook(String fileUrl) {
        try {
            String publicId = extractPublicIdFromUrl(fileUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            logger.info("File deleted successfully from Cloudinary: {}", fileUrl);
        } catch (Exception e) {
            logger.error("Failed to delete file: " + fileUrl, e);
            throw new RuntimeException("Failed to delete file from Cloudinary", e);
        }
    }
    // Helper method to extract public ID from Cloudinary URL
    private String extractPublicIdFromUrl(String fileUrl) {
        String[] parts = fileUrl.split("/");
        return parts[parts.length - 1].split("\\.")[0];
    }

}


package com.aues.library.service.impl;

import com.aues.library.controller.BookController;
import com.aues.library.exceptions.BookNotFoundException;
import com.aues.library.model.Book;
import com.aues.library.repository.BookRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private static final Logger logger = (Logger) LoggerFactory.getLogger(BookController.class);
    private final BookRepository bookRepository;


    public CloudinaryService(@Value("${cloudinary.cloud_name}") String cloudName,
                             @Value("${cloudinary.api_key}") String apiKey,
                             @Value("${cloudinary.api_secret}") String apiSecret, BookRepository bookRepository) {
        this.bookRepository = bookRepository;
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }

    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            logger.warn("No file provided for upload");
            return null;
        }
        logger.info("Uploading file: " + file.getOriginalFilename());
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return uploadResult.get("url").toString();
        } catch (IOException e) {
            logger.error("Cloudinary upload failed for file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to upload file to Cloudinary", e);
        }
    }



    public void deleteFile(String fileUrl) {
        try {
            String publicId = extractPublicIdFromUrl(fileUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            logger.error("Failed to delete file: " + fileUrl, e);
        }
    }

    private String extractPublicIdFromUrl(String fileUrl) {
        // Assuming the URL format, e.g., `https://res.cloudinary.com/<cloud_name>/image/upload/v<version>/<public_id>.<extension>`
        String[] parts = fileUrl.split("/");
        return parts[parts.length - 1].split("\\.")[0];
    }

    public String getFullPdfLink(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found"));
        return book.getFullPdf();
    }

    public byte[] downloadFile(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            try (InputStream inputStream = connection.getInputStream()) {
                return inputStream.readAllBytes();
            }
        } catch (IOException e) {
            logger.error("Failed to download file from Cloudinary: {}", fileUrl, e);
            throw new RuntimeException("Failed to download file", e);
        }
    }
}


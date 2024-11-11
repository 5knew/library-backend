package com.aues.library.service.impl;

import com.aues.library.controller.BookController;
import com.aues.library.exceptions.BookNotFoundException;
import com.aues.library.model.Book;
import com.aues.library.model.BookCopy;
import com.aues.library.model.FileMetadata;
import com.aues.library.repository.BookCopyRepository;
import com.aues.library.repository.BookRepository;
import com.aues.library.repository.FileMetadataRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final FileMetadataRepository fileMetadataRepository;

    public CloudinaryService(@Value("${cloudinary.cloud_name}") String cloudName,
                             @Value("${cloudinary.api_key}") String apiKey,
                             @Value("${cloudinary.api_secret}") String apiSecret,
                             BookRepository bookRepository,
                             BookCopyRepository bookCopyRepository,
                             FileMetadataRepository fileMetadataRepository) {
        this.bookRepository = bookRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.fileMetadataRepository = fileMetadataRepository;
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }

    public String uploadFile(MultipartFile file, Long bookCopyId) {
        if (file == null || file.isEmpty()) {
            logger.warn("No file provided for upload");
            return null;
        }
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

            FileMetadata metadata = new FileMetadata();
            metadata.setPublicId(uploadResult.get("public_id").toString());
            metadata.setResourceType(uploadResult.get("resource_type").toString());
            metadata.setUrl(uploadResult.get("url").toString());

            BookCopy bookCopy = bookCopyRepository.findById(bookCopyId)
                    .orElseThrow(() -> new BookNotFoundException("Book copy not found"));
            metadata.setBookCopy(bookCopy);

            fileMetadataRepository.save(metadata);

            return metadata.getUrl();
        } catch (IOException e) {
            logger.error("Failed to upload file to Cloudinary", e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    public byte[] downloadFileByBookCopy(Long bookCopyId) throws FileNotFoundException {
        List<FileMetadata> files = fileMetadataRepository.findByBookCopy_Id(bookCopyId);

        if (files.isEmpty()) {
            logger.warn("No FileMetadata found for BookCopy ID: {}", bookCopyId);
            throw new FileNotFoundException("No files found for this book copy");
        }

        FileMetadata fileMetadata = files.get(0);  // Adjust if you want to handle multiple files
        String fileUrl = fileMetadata.getUrl();

        logger.info("Attempting to download file from URL: {}", fileUrl);

        byte[] fileData = downloadFile(fileUrl);
        if (fileData == null || fileData.length == 0) {
            logger.warn("Downloaded file is empty for URL: {}", fileUrl);
        }
        return fileData;
    }


    private byte[] downloadFile(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream()) {
                    byte[] fileData = inputStream.readAllBytes();
                    logger.info("Downloaded file size: {} bytes", fileData.length);
                    return fileData;
                }
            } else {
                logger.error("Failed to download file from Cloudinary. Response code: {}", connection.getResponseCode());
                throw new RuntimeException("Failed to download file with status code: " + connection.getResponseCode());
            }
        } catch (IOException e) {
            logger.error("Failed to download file from Cloudinary: {}", fileUrl, e);
            throw new RuntimeException("Failed to download file", e);
        }
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

    // Download a file by its URL
    public byte[] downloadFileByUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream()) {
                    byte[] fileData = inputStream.readAllBytes();
                    logger.info("Downloaded file size: {} bytes", fileData.length);
                    return fileData;
                }
            } else {
                logger.error("Failed to download file from Cloudinary. Response code: {}", connection.getResponseCode());
                throw new RuntimeException("Failed to download file with status code: " + connection.getResponseCode());
            }
        } catch (IOException e) {
            logger.error("Failed to download file from Cloudinary: {}", fileUrl, e);
            throw new RuntimeException("Failed to download file", e);
        }
    }
}


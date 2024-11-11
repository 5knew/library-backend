package com.aues.library.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final String bucketName;

    public S3Service(@Value("${aws.accessKeyId}") String accessKey,
                     @Value("${aws.secretKey}") String secretKey,
                     @Value("${aws.region}") String region,
                     @Value("${aws.s3.bucketName}") String bucketName) {
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
        this.bucketName = bucketName;
    }

    public String uploadFile(MultipartFile multipartFile, String path) {
        String key = path;
        File file = convertMultipartFileToFile(multipartFile);
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build(),
                    file.toPath());
            return s3Client.utilities().getUrl(GetUrlRequest.builder().bucket(bucketName).key(key).build()).toExternalForm();
        } finally {
            file.delete();
        }
    }

    private File convertMultipartFileToFile(MultipartFile multipartFile) {
        File file = new File(multipartFile.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error converting multipart file to file", e);
        }
        return file;
    }

    public byte[] downloadFile(String url) {
        // Extract key from the full URL
        String key = extractKeyFromUrl(url);
        System.out.println("Attempting to download file with S3 key: " + key);

        try {
            // Use the extracted key for the S3 download request
            ResponseBytes<GetObjectResponse> s3Object = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            System.out.println("Successfully downloaded file from S3 with key: " + key);
            return s3Object.asByteArray();
        } catch (S3Exception e) {
            System.err.println("Failed to download file from S3. Key: " + key + ", Error: " + e.awsErrorDetails().errorMessage());
            return null;
        }
    }


    public void deleteFile(String fileKey) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build());
        System.out.println("Deleted file from S3: " + fileKey);
    }

    public String extractKeyFromUrl(String fileUrl) {
        return fileUrl.substring(fileUrl.indexOf(".com/") + 5);
    }

}


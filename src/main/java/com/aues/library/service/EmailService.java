package com.aues.library.service;

import com.aues.library.service.impl.S3Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;
import java.util.List;


@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private S3Service s3Service;



    /**
     * Sends a simple email with plain text.
     */
    // Отправка email без вложений
    public void sendEmail(String to, String subject, String text) {
        sendEmailWithAttachmentsFromS3(to, subject, text, Collections.emptyList(), "Attachment");
    }

    public void sendEmailWithAttachmentsFromS3(String to, String subject, String text, List<String> s3Keys, String baseName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);

            if (s3Keys != null && !s3Keys.isEmpty()) {
                int index = 1;
                for (String key : s3Keys) {
                    System.out.println("Attempting to download file with S3 key: " + key);

                    // Download file from S3
                    byte[] fileData = s3Service.downloadFile(key);
                    if (fileData != null && fileData.length > 0) {
                        helper.addAttachment(baseName + index + ".pdf", new ByteArrayResource(fileData));
                        index++;
                    } else {

                        logger.warn("File data is null or empty for key: {}", key);
                    }
                }
            }

            mailSender.send(message);
            logger.info("Email with attachments sent successfully to {}", to);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email with attachments", e);
        }
    }


}

package com.aues.library.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Sends a simple email with plain text.
     */
    public void sendEmail(String to, String subject, String text) {
        sendEmailWithOptionalAttachments(to, subject, text, false, null);
    }

    /**
     * Sends an email with HTML support and optional attachments.
     */
    public void sendEmailWithOptionalAttachments(String to, String subject, String text, boolean isHtml,
                                                 Map<String, InputStreamSource> attachments) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, attachments != null && !attachments.isEmpty());

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, isHtml);

            if (attachments != null) {
                attachments.forEach((name, source) -> {
                    try {
                        helper.addAttachment(name, source);
                    } catch (MessagingException e) {
                        logger.error("Failed to add attachment: {}", name, e);
                    }
                });
            }

            mailSender.send(message);
            logger.info("Email sent to {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Sends an email with multiple PDF attachments.
     */
    public void sendEmailWithMultiplePdfAttachments(String to, String subject, String text, List<byte[]> pdfDataList,
                                                    String basePdfName) {
        Map<String, InputStreamSource> pdfAttachments = createPdfAttachments(pdfDataList, basePdfName);
        sendEmailWithOptionalAttachments(to, subject, text, false, pdfAttachments);
    }

    /**
     * Helper method to create PDF attachments from byte data.
     */
    private Map<String, InputStreamSource> createPdfAttachments(List<byte[]> pdfDataList, String basePdfName) {
        return pdfDataList.stream().collect(Collectors.toMap(
                data -> basePdfName + (pdfDataList.indexOf(data) + 1) + ".pdf",
                ByteArrayResource::new
        ));
    }
}

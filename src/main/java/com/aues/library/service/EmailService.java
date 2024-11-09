package com.aues.library.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    // Existing sendEmailWithPdfAttachment method
    public void sendEmailWithMultiplePdfAttachments(String to, String subject, String text, List<byte[]> pdfDataList, String basePdfName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);

            int index = 1;
            for (byte[] pdfData : pdfDataList) {
                helper.addAttachment(basePdfName + index + ".pdf", new ByteArrayResource(pdfData));
                index++;
            }

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email with PDF attachments", e);
        }
    }

}

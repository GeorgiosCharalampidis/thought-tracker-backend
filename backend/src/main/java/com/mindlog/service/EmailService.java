package com.mindlog.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.base-url}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String toAddress, String username, String token) {
        String link = baseUrl + "/verify-email?token=" + token;
        String subject = "Verify your MindLog email";
        String body = "<p>Hi " + username + ",</p>"
                + "<p>Thank you for signing up to MindLog. Please verify your email by clicking the link below:</p>"
                + "<p><a href=\"" + link + "\">Verify my email</a></p>"
                + "<p>This link expires in 24 hours.</p>"
                + "<p>If you did not create an account, you can safely ignore this email.</p>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toAddress);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            logger.info("Verification email sent to {}", toAddress);
        } catch (MessagingException | MailException e) {
            logger.error("Failed to send verification email to {}: {}", toAddress, e.getMessage());
            throw new RuntimeException("Could not send verification email. Please try again later.");
        }
    }
}

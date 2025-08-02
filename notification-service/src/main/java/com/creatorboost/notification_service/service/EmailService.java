package com.creatorboost.notification_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;


    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    public void sendWelcomeEmail(String toEmail, String name) {

        Context context = new Context();
        context.setVariable("name", name);

        String htmlContent = templateEngine.process("welcome-email", context);
        sendHtmlEmail(toEmail, "Welcome to CreatorBoost!", htmlContent);
        /*SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom(fromEmail);
        message.setSubject("Welcome to CreatorBoost!");
        message.setText("Hello " + name + ",\n\n" +
                "Welcome to CreatorBoost! We're excited to have you on board.\n\n" +
                "Best regards,\n" +
                "The CreatorBoost Team");
        mailSender.send(message);*/
    }

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        Context context = new Context();
        context.setVariable("resetLink", resetLink);
        context.setVariable("emailType", "password reset");

        String htmlContent = templateEngine.process("reset-password", context);
        sendHtmlEmail(toEmail, "Password Reset", htmlContent);
    }

    public void sendOtpEmail(String toEmail, String otp) {
        Context context = new Context();
        context.setVariable("otp", otp);
        context.setVariable("emailType", "account verification");

        String htmlContent = templateEngine.process("otp-email", context);
        sendHtmlEmail(toEmail, "Account Verification OTP", htmlContent);
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = isHtml

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}

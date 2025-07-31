package com.creatorboost.auth_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    public void sendWelcomeEmail(String toEmail, String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom(fromEmail);
        message.setSubject("Welcome to CreatorBoost!");
        message.setText("Hello " + name + ",\n\n" +
                "Welcome to CreatorBoost! We're excited to have you on board.\n\n" +
                "Best regards,\n" +
                "The CreatorBoost Team");
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom(fromEmail);
        message.setSubject("Password Reset OTP");
        message.setText("Your OTP for password reset is: " + otp + "\n\n" +
                "Please use this OTP to reset your password. If you did not request a password reset, please ignore this email.\n\n" +
                "Best regards,\n" +
                "The CreatorBoost Team");
        mailSender.send(message);
    }

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom(fromEmail);
        message.setSubject("Account Verification OTP");
        message.setText("Your OTP code is: " + otp + "\n\n" +
                "Please use this code to complete your action. If you did not request this, please ignore this email.\n\n" +
                "Best regards,\n" +
                "The CreatorBoost Team");
        mailSender.send(message);
    }
}

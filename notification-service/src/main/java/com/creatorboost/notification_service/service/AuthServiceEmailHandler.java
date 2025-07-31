package com.creatorboost.notification_service.service;

import dto.EmailMessageDto;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceEmailHandler implements EmailHandler {


    @Override
    public boolean canHandle(EmailMessageDto message) {
        return "auth-service".equals(message.getServiceFrom());
    }

    @Override
    public void handle(EmailMessageDto message, EmailService emailService) {
        switch(message.getEmailType()) {
            case "WELCOME":
                emailService.sendWelcomeEmail(message.getToEmail(), message.getName());
                break;
            case "PASSWORD_RESET_OTP":
                emailService.sendPasswordResetEmail(message.getToEmail(), message.getOtp());
                break;
            case "VERIFICATION_OTP":
                emailService.sendOtpEmail(message.getToEmail(), message.getOtp());
                break;
            default:
                throw new IllegalArgumentException("Unsupported email type from auth-service: " + message.getEmailType());
        }
    }


}
package com.creatorboost.notification_service.service;

import dto.EmailMessageDto;

public interface EmailHandler {
    boolean canHandle(EmailMessageDto message);
    void handle(EmailMessageDto message, EmailService emailService);
}


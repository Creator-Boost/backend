/*package com.creatorboost.notification_service.service;

import dto.EmailMessageDto;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceEmailHandler implements EmailHandler {

    @Override
    public boolean canHandle(EmailMessageDto message) {
        return "order-service".equals(message.getServiceFrom());
    }
/*
    @Override
    public void handle(EmailMessageDto message, EmailService emailService) {
        switch(message.getEmailType()) {
            case "ORDER_CONFIRMATION":
                emailService.sendOrderConfirmation(message.getToEmail(), message.getRecipientName());
                break;
            case "SHIPPING_UPDATE":
                emailService.sendShippingUpdate(message.getToEmail(), message.getRecipientName());
                break;
            default:
                throw new IllegalArgumentException("Unsupported email type from order-service: " + message.getEmailType());
        }
    }

}*/
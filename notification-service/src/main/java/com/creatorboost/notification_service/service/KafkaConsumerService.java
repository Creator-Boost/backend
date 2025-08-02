package com.creatorboost.notification_service.service;

import com.creatorboost.notification_service.factory.EmailHandlerFactory;
import dto.EmailMessageDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    private  final EmailHandlerFactory handlerFactory;
    private final EmailService emailService;

    @KafkaListener(topics = "email_default_topic", groupId = "notification", containerFactory = "kafkaListenerContainerFactory")
    public void consumeEmailMessage(EmailMessageDto emailMessage) {
        try {
            logger.info("Received email from {} service, type: {}",
                    emailMessage.getServiceFrom(), emailMessage.getEmailType());

            EmailHandler handler = handlerFactory.getHandler(emailMessage);
            handler.handle(emailMessage, emailService);

            logger.info("Successfully processed email from {} service",
                    emailMessage.getServiceFrom());
        } catch (Exception e) {
            logger.error("Error processing email from {} service: {}",
                    emailMessage.getServiceFrom(), emailMessage, e);
        }
    }
}
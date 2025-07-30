package com.creatorboost.notification_service.service;

import dto.MessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    @KafkaListener(topics = "email_default_topic", groupId = "notification", containerFactory = "kafkaListenerContainerFactory")
    public void consumeMessage(MessageDto messageDto) {
        try {
            logger.info("🎯 NOTIFICATION SERVICE: Parsed message successfully");
            logger.info("📋 NOTIFICATION SERVICE: Message Details:");
            logger.info("   ├── Content: {}", messageDto.getMessage());
            logger.info("   ├── From: {}", messageDto.getServiceFrom());
            logger.info("   └── Timestamp: {}", messageDto.getTimestamp());

            processNotification(messageDto);

            logger.info("✅ NOTIFICATION SERVICE: Message processed successfully");

        } catch (Exception e) {
            logger.error("❌ NOTIFICATION SERVICE: Error processing message: {}", messageDto, e);
        }
    }

    private void processNotification(MessageDto messageDto) {
        logger.info("🔔 NOTIFICATION SERVICE: Processing notification for message: {}", messageDto.getMessage());
        logger.info("📧 NOTIFICATION SERVICE: Notification sent successfully for message from {}", messageDto.getServiceFrom());
    }
}

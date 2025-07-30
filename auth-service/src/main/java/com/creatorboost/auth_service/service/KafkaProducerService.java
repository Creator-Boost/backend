package com.creatorboost.auth_service.service;

import com.creatorboost.auth_service.io.MessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.template.default-topic}")
    private String topicName;

    public void sendMessage(String message) {
        try {
            // Create message DTO
            dto.MessageDto messageDto = new dto.MessageDto(
                    message,
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    "auth-service"
            );

            logger.info("🚀 AUTH SERVICE: Preparing to send message to topic: {}", topicName);
            logger.info("📤 AUTH SERVICE: Message content: {}", messageDto);

            // Send message
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topicName, messageDto);

            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    logger.info("✅ AUTH SERVICE: Message sent successfully to topic: {} with offset: {}",
                            topicName, result.getRecordMetadata().offset());
                } else {
                    logger.error("❌ AUTH SERVICE: Failed to send message to topic: {}", topicName, exception);
                }
            });

        } catch (Exception e) {
            logger.error("💥 AUTH SERVICE: Error occurred while sending message", e);
        }
    }
}

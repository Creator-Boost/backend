package com.creatorboost.auth_service.controller;

import com.creatorboost.auth_service.service.KafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @PostMapping("/send-message")
    public ResponseEntity<String> sendTestMessage(@RequestParam(name = "message", defaultValue = "Hello from Auth Service!") String message) {
        logger.info("🎯 AUTH SERVICE: Received request to send message: {}", message);

        kafkaProducerService.sendMessage(message);

        String response = "Message sent to Kafka topic successfully: " + message;
        logger.info("✨ AUTH SERVICE: {}", response);

        return ResponseEntity.ok(response);
    }
}

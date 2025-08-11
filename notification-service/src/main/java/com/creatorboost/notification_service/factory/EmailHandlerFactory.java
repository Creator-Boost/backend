package com.creatorboost.notification_service.factory;

import com.creatorboost.notification_service.service.EmailHandler;
import dto.EmailMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailHandlerFactory {
    private final List<EmailHandler> handlers;

    public EmailHandler getHandler(EmailMessageDto message) {
        return handlers.stream()
                .filter(h -> h.canHandle(message))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No handler found for service: " + message.getServiceFrom()));
    }
}
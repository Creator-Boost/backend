package com.creatorboost.auth_service.io;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDto {
    private String message;
    private String timestamp;
    private String serviceFrom;

    public MessageDto() {}

    public MessageDto(String message, String timestamp, String serviceFrom) {
        this.message = message;
        this.timestamp = timestamp;
        this.serviceFrom = serviceFrom;
    }


    @Override
    public String toString() {
        return "MessageDto{" +
                "message='" + message + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", serviceFrom='" + serviceFrom + '\'' +
                '}';
    }
}
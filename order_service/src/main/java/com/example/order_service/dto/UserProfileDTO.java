package com.example.order_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class UserProfileDTO {
    private UUID userId;
    private String name;
    private String email;
    private String role;
    private String imageUrl;
    private Object providerProfile;
    private Object clientProfile;
    private LocalDateTime createdAt;
    private boolean accountVerified;
}

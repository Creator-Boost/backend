package com.example.order_service.dto;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class OrderRequestDTO {

    private UUID gigId;
    private UUID packageId; // Changed from gigPackageId to packageId for better API consistency
    private UUID buyerId;
    private UUID sellerId;
    private String requirements;
    private LocalDateTime deliveryDate;

// Getters and Setters
}

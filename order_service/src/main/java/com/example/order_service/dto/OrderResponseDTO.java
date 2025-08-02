package com.example.order_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class OrderResponseDTO {

    private UUID id;
    private UUID gigId;
    private UUID gigPackageId;
    private UUID buyerId;
    private UUID sellerId;
    private String requirements;
    private String status;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;

    // Getters and Setters
}

package com.example.order_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
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

    // Add financial and package information
    private BigDecimal amount;
    private String packageName;

    private String requirements;
    private String status;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;

    // Add gig details for better frontend experience
    private String gigTitle;
    private String gigDescription;
    private String packageDescription;

    // Getters and Setters
}

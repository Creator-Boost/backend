package com.example.order_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class PaymentDTO {

    private UUID orderId;
    private BigDecimal amount;

    // Getters and Setters
}

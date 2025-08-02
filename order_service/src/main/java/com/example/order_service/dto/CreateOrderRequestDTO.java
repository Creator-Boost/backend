package com.example.order_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateOrderRequestDTO {
    private UUID gigId;
    private UUID packageId;
    private UUID buyerId;
    private String requirements;
}

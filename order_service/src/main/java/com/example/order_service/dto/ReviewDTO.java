package com.example.order_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ReviewDTO {

    private UUID orderId;
    private Integer rating;
    private String reviewText;

    // Getters and Setters
}

package com.example.gig_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class GigPackageDTO {
    private String name;
    private BigDecimal price;
    private int deliveryDays;
    private String description;
    // Getters & Setters
}

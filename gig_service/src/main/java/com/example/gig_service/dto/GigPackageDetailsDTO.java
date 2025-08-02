package com.example.gig_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class GigPackageDetailsDTO {
    private UUID packageId;
    private UUID gigId;
    private String name;
    private BigDecimal price;
    private int deliveryDays;
    private String description;
}

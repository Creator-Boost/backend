package com.example.order_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class GigDetailsDTO {
    private UUID id;
    private String title;
    private String description;
    private UUID sellerId;
    private String category;
    private String status;
    private List<GigPackageDTO> packages;

    @Getter
    @Setter
    public static class GigPackageDTO {
        private UUID id;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer deliveryDays;
    }
}

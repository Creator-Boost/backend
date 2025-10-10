package com.example.order_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class DisputeDTO {
    private UUID id;
    private String title;
    private String description;
    private LocalDateTime createdDate;
    private UUID userId; // ID of the user who created the dispute
    private Boolean resolved; // Whether the dispute is resolved

    // Constructor for creating new disputes
    public DisputeDTO() {}

    // Constructor with title and description for creation
    public DisputeDTO(String title, String description) {
        this.title = title;
        this.description = description;
        this.resolved = false; // Default to unresolved
    }
}

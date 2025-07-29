package com.example.gig_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class GigResponseDTO {
    // --- Getters and Setters ---
    private UUID id;
    private UUID sellerId;
    private String title;
    private String description;
    private String platform;
    private String category;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<GigImageDTO> images;
    private List<GigPackageDTO> packages;
    private List<GigFaqDTO> faqs;
}


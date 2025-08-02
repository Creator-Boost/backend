package com.example.gig_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class GigWithPackageDetailsDTO {
    // Gig details
    private UUID gigId;
    private UUID sellerId;
    private String title;
    private String description;
    private String platform;
    private String category;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // All gig images
    private List<GigImageDTO> images;

    // All gig FAQs
    private List<GigFaqDTO> faqs;

    // All packages in the gig
    private List<GigPackageDTO> allPackages;

    // Specific package details
    private GigPackageDetailsDTO selectedPackage;
}

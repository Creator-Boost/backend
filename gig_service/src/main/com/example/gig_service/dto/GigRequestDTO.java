package src.main.com.example.gig_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class GigRequestDTO {
    // --- Getters and Setters ---
    private UUID sellerId;
    private String title;
    private String description;
    private String platform;
    private String category;
    private String status; // "ACTIVE", "PAUSED"
    private List<GigImageDTO> images;
    private List<GigPackageDTO> packages;
    private List<GigFaqDTO> faqs;

}
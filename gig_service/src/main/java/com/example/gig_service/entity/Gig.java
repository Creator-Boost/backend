package com.example.gig_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Entity
public class Gig {
    // --- Getters and Setters ---
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private UUID sellerId;
    private String title;
    @Column(length = 2000)
    private String description;
    private String platform;
    private String category;
    private String status; // "ACTIVE", "PAUSED"

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // === Relationships ===

    @OneToMany(mappedBy = "gig", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<GigImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "gig", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<GigPackage> packages = new ArrayList<>();

    @OneToMany(mappedBy = "gig", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<GigFaq> faqs = new ArrayList<>();


    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}



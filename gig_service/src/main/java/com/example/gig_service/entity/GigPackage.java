package com.example.gig_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "gig_packages")
public class GigPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gig_id", nullable = false)
    private Gig gig;

    private String name; // e.g., Basic, Standard, Premium
    private BigDecimal price;
    private int deliveryDays;
    private String description;

    // Getters and Setters
}

package com.example.order_service.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID gigId;
    private UUID gigPackageId;
    private UUID buyerId;
    private UUID sellerId;

    // Add amount field to store the order amount
    private BigDecimal amount;
    private String packageName;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'PENDING'")
    private AdminStatus adminStatus = AdminStatus.PENDING;

    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;

    @Column(columnDefinition = "TEXT")
    private String deliveredFiles; // URLs of the files delivered by the seller

    // JPA Relationships
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Review review;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Dispute> disputes;

    public enum OrderStatus {
        NEW, IN_PROGRESS, DELIVERED, COMPLETED, PAID, CANCELED
    }

    public enum AdminStatus {
        PENDING, PAID, REFUNDED
    }

    @PrePersist
    protected void onCreate() {
        if (adminStatus == null) {
            adminStatus = AdminStatus.PENDING;
        }
    }

    // Getters and Setters
}

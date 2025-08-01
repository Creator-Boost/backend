package com.example.order_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    @Lob
    private String requirements;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;

    @Lob
    private String deliveredFiles; // URLs of the files delivered by the seller

    // JPA Relationships
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Review review;

    public enum OrderStatus {
        NEW, IN_PROGRESS, DELIVERED, COMPLETED, PAID, CANCELED
    }

    // Getters and Setters
}

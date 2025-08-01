package com.example.order_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID gigId;
    private UUID reviewerId;

    // JPA Relationship - One-to-One with Order
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private Integer rating;

    @Lob
    private String reviewText;

    private LocalDateTime createdAt;

    // Helper method to get orderId for backward compatibility
    public UUID getOrderId() {
        return order != null ? order.getId() : null;
    }

    // Helper method to set orderId for backward compatibility
    public void setOrderId(UUID orderId) {
        if (this.order == null) {
            this.order = new Order();
        }
        this.order.setId(orderId);
    }

    // Getters and Setters
}

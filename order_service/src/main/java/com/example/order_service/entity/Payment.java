package com.example.order_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // JPA Relationship - Many-to-One with Order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime paymentDate;
    private String transactionId;

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

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED
    }

    // Getters and Setters
}

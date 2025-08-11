package com.example.order_service.controller;

import com.example.order_service.dto.CreateOrderRequestDTO;
import com.example.order_service.dto.OrderRequestDTO;
import com.example.order_service.dto.OrderResponseDTO;
import com.example.order_service.dto.PaymentDTO;
import com.example.order_service.dto.ReviewDTO;
import com.example.order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // Create a new order
    @PostMapping
    public OrderResponseDTO createOrder(@RequestBody OrderRequestDTO orderDTO) {
        return orderService.createOrder(orderDTO);
    }

    /**
     * Create order from gig - Frontend sends minimal info, order service validates with gig service
     * This is the main endpoint for the user flow: Browse gigs -> Choose package -> Click "Order"
     */
    @PostMapping("/from-gig")
    public OrderResponseDTO createOrderFromGig(@RequestBody CreateOrderRequestDTO createOrderRequest) {
        try {
            return orderService.createOrderFromGig(createOrderRequest);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
        }
    }

    // Process payment for an order
    @PostMapping("/{id}/payment")
    public OrderResponseDTO processPayment(@PathVariable UUID id, @RequestBody PaymentDTO paymentDTO) {
        return orderService.processPayment(id, paymentDTO);
    }

    // Add a review to an order
    @PostMapping("/{id}/review")
    public ReviewDTO addReview(@PathVariable UUID id, @RequestBody ReviewDTO reviewDTO) {
        return orderService.addReview(id, reviewDTO);
    }

}

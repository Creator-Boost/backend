package com.example.order_service.controller;

import com.example.order_service.dto.*;
import com.example.order_service.entity.Order;
import com.example.order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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

    // Get all orders
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    // Get specific order by ID
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable UUID orderId) {
        Order order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    // Update order status
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
        @PathVariable UUID orderId,
        @RequestBody UpdateStatusRequest request
    ) {
        Order updatedOrder = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(updatedOrder);
    }

    // Get orders by buyer ID
    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<Order>> getOrdersByBuyer(@PathVariable UUID buyerId) {
        List<Order> orders = orderService.getOrdersByBuyer(buyerId);
        return ResponseEntity.ok(orders);
    }

    // Get orders by seller ID
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<Order>> getOrdersBySeller(@PathVariable UUID sellerId) {
        List<Order> orders = orderService.getOrdersBySeller(sellerId);
        return ResponseEntity.ok(orders);
    }

    // Add delivery files (for seller)
    @PostMapping("/{orderId}/delivery-files")
    public ResponseEntity<Order> addDeliveryFiles(
        @PathVariable UUID orderId,
        @RequestParam("files") List<MultipartFile> files
    ) {
        Order updatedOrder = orderService.addDeliveryFiles(orderId, files);
        return ResponseEntity.ok(updatedOrder);
    }

    // Update order requirements (for buyer)
    @PatchMapping("/{orderId}/requirements")
    public ResponseEntity<Order> updateRequirements(
        @PathVariable UUID orderId,
        @RequestBody UpdateRequirementsRequest request
    ) {
        Order updatedOrder = orderService.updateRequirements(orderId, request);
        return ResponseEntity.ok(updatedOrder);
    }

    // Cancel order
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable UUID orderId) {
        Order canceledOrder = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(canceledOrder);
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

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
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByBuyer(@PathVariable UUID buyerId) {
        List<OrderResponseDTO> orders = orderService.getOrdersByBuyerWithNames(buyerId);
        return ResponseEntity.ok(orders);
    }

    // Get orders by seller ID
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersBySeller(@PathVariable UUID sellerId) {
        List<OrderResponseDTO> orders = orderService.getOrdersBySellerWithNames(sellerId);
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

    // Delete all orders (Admin functionality)
    @DeleteMapping("/all")
    public ResponseEntity<String> deleteAllOrders() {
        orderService.deleteAllOrders();
        return ResponseEntity.ok("All orders have been deleted successfully");
    }

    // Dispute-related endpoints

    /**
     * Add a dispute to an order
     * Both buyer and seller can create disputes for an order
     */
    @PostMapping("/{orderId}/disputes")
    public ResponseEntity<DisputeDTO> addDispute(
        @PathVariable UUID orderId,
        @RequestBody DisputeDTO disputeDTO,
        @RequestParam UUID userId // In a real app, this would come from authentication context
    ) {
        DisputeDTO createdDispute = orderService.addDispute(orderId, disputeDTO, userId);
        return ResponseEntity.ok(createdDispute);
    }

    /**
     * Get all disputes for a specific order
     */
    @GetMapping("/{orderId}/disputes")
    public ResponseEntity<List<DisputeDTO>> getDisputesByOrderId(@PathVariable UUID orderId) {
        List<DisputeDTO> disputes = orderService.getDisputesByOrderId(orderId);
        return ResponseEntity.ok(disputes);
    }

    /**
     * Get order with all its data including disputes
     */
    @GetMapping("/{orderId}/with-disputes")
    public ResponseEntity<Order> getOrderWithDisputes(@PathVariable UUID orderId) {
        Order order = orderService.getOrderWithDisputes(orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * Update dispute status (resolve/unresolve)
     */
    @PatchMapping("/disputes/{disputeId}/status")
    public ResponseEntity<DisputeDTO> updateDisputeStatus(
        @PathVariable UUID disputeId,
        @RequestBody UpdateDisputeStatusRequest request
    ) {
        DisputeDTO updatedDispute = orderService.updateDisputeStatus(disputeId, request);
        return ResponseEntity.ok(updatedDispute);
    }

    /**
     * Update admin status of an order (Admin functionality)
     * Only admins should have access to this endpoint
     */
    @PatchMapping("/{orderId}/admin-status")
    public ResponseEntity<Order> updateAdminStatus(
        @PathVariable UUID orderId,
        @RequestBody UpdateAdminStatusRequest request
    ) {
        Order updatedOrder = orderService.updateAdminStatus(orderId, request);
        return ResponseEntity.ok(updatedOrder);
    }

}

package com.example.order_service.service;

import com.example.order_service.dto.OrderRequestDTO;
import com.example.order_service.dto.OrderResponseDTO;
import com.example.order_service.dto.PaymentDTO;
import com.example.order_service.dto.ReviewDTO;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.Payment;
import com.example.order_service.entity.Review;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.repository.PaymentRepository;
import com.example.order_service.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    // Create a new order
    public OrderResponseDTO createOrder(OrderRequestDTO orderDTO) {
        Order order = new Order();
        order.setGigId(orderDTO.getGigId());
        order.setGigPackageId(orderDTO.getGigPackageId());
        order.setBuyerId(orderDTO.getBuyerId());
        order.setSellerId(orderDTO.getSellerId());
        order.setRequirements(orderDTO.getRequirements());
        order.setStatus(Order.OrderStatus.NEW);
        order.setOrderDate(java.time.LocalDateTime.now());
        order.setDeliveryDate(orderDTO.getDeliveryDate());

        Order savedOrder = orderRepository.save(order);
        return mapToOrderResponseDTO(savedOrder);
    }

    // Process payment
    public OrderResponseDTO processPayment(UUID orderId, PaymentDTO paymentDTO) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(paymentDTO.getAmount());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setPaymentDate(java.time.LocalDateTime.now());

        // Simulate payment confirmation
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        paymentRepository.save(payment);

        // Update order status
        order.setStatus(Order.OrderStatus.PAID);
        orderRepository.save(order);

        return mapToOrderResponseDTO(order);
    }

    // Add a review to an order
    public ReviewDTO addReview(UUID orderId, ReviewDTO reviewDTO) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

        Review review = new Review();
        review.setOrderId(orderId);
        review.setGigId(order.getGigId());
        review.setReviewerId(order.getBuyerId());
        review.setRating(reviewDTO.getRating());
        review.setReviewText(reviewDTO.getReviewText());
        review.setCreatedAt(java.time.LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        // Map the saved Review entity to ReviewDTO
        ReviewDTO responseDTO = new ReviewDTO();
        responseDTO.setOrderId(savedReview.getOrderId());
        responseDTO.setRating(savedReview.getRating());
        responseDTO.setReviewText(savedReview.getReviewText());

        return responseDTO;
    }

    private OrderResponseDTO mapToOrderResponseDTO(Order order) {
        OrderResponseDTO response = new OrderResponseDTO();
        response.setId(order.getId());
        response.setGigId(order.getGigId());
        response.setGigPackageId(order.getGigPackageId());
        response.setBuyerId(order.getBuyerId());
        response.setSellerId(order.getSellerId());
        response.setRequirements(order.getRequirements());
        response.setStatus(order.getStatus().toString());
        response.setOrderDate(order.getOrderDate());
        response.setDeliveryDate(order.getDeliveryDate());
        return response;
    }
}

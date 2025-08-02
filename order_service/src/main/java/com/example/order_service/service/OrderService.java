package com.example.order_service.service;

import com.example.order_service.dto.*;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.Payment;
import com.example.order_service.entity.Review;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.repository.PaymentRepository;
import com.example.order_service.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private GigServiceClient gigServiceClient;

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

    /**
     * Create a new order with gig service validation
     * This method validates gig/package existence and retrieves trusted details from gig service
     */
    public OrderResponseDTO createOrderFromGig(CreateOrderRequestDTO createOrderRequest) {
        // Step 1: Verify that gig and package exist and belong together
        boolean exists = gigServiceClient.verifyGigAndPackageExists(
            createOrderRequest.getGigId(),
            createOrderRequest.getPackageId()
        );

        if (!exists) {
            throw new RuntimeException("Gig or package not found, or package does not belong to the specified gig");
        }

        // Step 2: Get trusted gig and package details from gig service
        GigWithPackageDetailsDTO gigDetails = gigServiceClient.getGigWithPackageDetails(
            createOrderRequest.getGigId(),
            createOrderRequest.getPackageId()
        );

        // Step 3: Validate gig status (ensure it's active)
        if (!"ACTIVE".equalsIgnoreCase(gigDetails.getStatus())) {
            throw new RuntimeException("Gig is not active and cannot be ordered");
        }

        // Step 4: Calculate delivery date based on package delivery days
        LocalDateTime deliveryDate = LocalDateTime.now().plusDays(gigDetails.getSelectedPackage().getDeliveryDays());

        // Step 5: Create order with trusted data from gig service
        Order order = new Order();
        order.setGigId(createOrderRequest.getGigId());
        order.setGigPackageId(createOrderRequest.getPackageId());
        order.setBuyerId(createOrderRequest.getBuyerId());
        order.setSellerId(gigDetails.getSellerId()); // Trusted seller ID from gig service
        order.setAmount(gigDetails.getSelectedPackage().getPrice()); // Trusted price from gig service
        order.setPackageName(gigDetails.getSelectedPackage().getName());
        order.setRequirements(createOrderRequest.getRequirements());
        order.setStatus(Order.OrderStatus.NEW);
        order.setOrderDate(LocalDateTime.now());
        order.setDeliveryDate(deliveryDate);

        // Step 6: Save order to database
        Order savedOrder = orderRepository.save(order);

        // Step 7: Return order response with additional gig details
        return mapToOrderResponseDTOWithGigDetails(savedOrder, gigDetails);
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

    private OrderResponseDTO mapToOrderResponseDTOWithGigDetails(Order order, GigWithPackageDetailsDTO gigDetails) {
        OrderResponseDTO response = new OrderResponseDTO();
        response.setId(order.getId());
        response.setGigId(order.getGigId());
        response.setGigPackageId(order.getGigPackageId());
        response.setBuyerId(order.getBuyerId());
        response.setSellerId(order.getSellerId());
        response.setAmount(order.getAmount());
        response.setPackageName(order.getPackageName());
        response.setRequirements(order.getRequirements());
        response.setStatus(order.getStatus().toString());
        response.setOrderDate(order.getOrderDate());
        response.setDeliveryDate(order.getDeliveryDate());

        // Add gig details for better frontend experience
        response.setGigTitle(gigDetails.getTitle());
        response.setGigDescription(gigDetails.getDescription());
        response.setPackageDescription(gigDetails.getSelectedPackage().getDescription());

        return response;
    }
}

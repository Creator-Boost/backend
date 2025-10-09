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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private GigServiceClient gigServiceClient;

    @Autowired
    private AuthServiceClient authServiceClient;

    @Autowired
    private FileUploadService fileUploadService;

    // Create a new order
    public OrderResponseDTO createOrder(OrderRequestDTO orderDTO) {
        // Fetch gig details from gig service
        GigDetailsDTO gigDetails = gigServiceClient.getGigDetails(orderDTO.getGigId());

        System.out.println("DEBUG: Fetched gig details - Status: " + gigDetails.getStatus());
        System.out.println("DEBUG: Number of packages: " + (gigDetails.getPackages() != null ? gigDetails.getPackages().size() : "null"));

        // Validate gig status (ensure it's active)
        if (!"ACTIVE".equalsIgnoreCase(gigDetails.getStatus())) {
            throw new RuntimeException("Gig is not active and cannot be ordered");
        }

        // Find the package details if packageId is provided
        GigDetailsDTO.GigPackageDTO selectedPackage = null;
        if (orderDTO.getPackageId() != null) {
            System.out.println("DEBUG: Looking for package ID: " + orderDTO.getPackageId());

            if (gigDetails.getPackages() != null) {
                for (GigDetailsDTO.GigPackageDTO pkg : gigDetails.getPackages()) {
                    System.out.println("DEBUG: Available package - ID: " + pkg.getId() + ", Name: " + pkg.getName() + ", Price: " + pkg.getPrice());
                }
            }

            selectedPackage = gigDetails.getPackages().stream()
                .filter(pkg -> pkg.getId().equals(orderDTO.getPackageId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Package not found with ID: " + orderDTO.getPackageId()));

            System.out.println("DEBUG: Selected package - Name: " + selectedPackage.getName() + ", Price: " + selectedPackage.getPrice());
        }

        Order order = new Order();
        order.setGigId(orderDTO.getGigId());
        order.setGigPackageId(orderDTO.getPackageId()); // Using packageId from DTO
        order.setBuyerId(orderDTO.getBuyerId());
        order.setSellerId(gigDetails.getSellerId()); // Get sellerId from gig details
        order.setRequirements(orderDTO.getRequirements());
        order.setStatus(Order.OrderStatus.NEW);
        order.setOrderDate(java.time.LocalDateTime.now());

        // Set package-specific details if package is selected
        if (selectedPackage != null) {
            System.out.println("DEBUG: Setting package details - Name: " + selectedPackage.getName() + ", Price: " + selectedPackage.getPrice());
            order.setAmount(selectedPackage.getPrice());
            order.setPackageName(selectedPackage.getName());
            // Calculate delivery date based on package delivery days
            order.setDeliveryDate(java.time.LocalDateTime.now().plusDays(selectedPackage.getDeliveryDays()));
        } else {
            System.out.println("DEBUG: No package selected, using provided delivery date");
            // Use provided delivery date if no package is selected
            order.setDeliveryDate(orderDTO.getDeliveryDate());
        }

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

    // Get order by ID
    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }

    // Update order status
    public Order updateOrderStatus(UUID orderId, UpdateStatusRequest request) {
        Order order = getOrderById(orderId);

        // Add validation logic for status transitions if needed
        validateStatusTransition(order.getStatus(), request.getStatus());

        order.setStatus(request.getStatus());
        return orderRepository.save(order);
    }

    // Get all orders
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Get orders by buyer ID with user names
    public List<OrderResponseDTO> getOrdersByBuyerWithNames(UUID buyerId) {
        List<Order> orders = orderRepository.findByBuyerId(buyerId);
        return orders.stream()
                .map(this::mapToOrderResponseDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    // Get orders by seller ID with user names
    public List<OrderResponseDTO> getOrdersBySellerWithNames(UUID sellerId) {
        List<Order> orders = orderRepository.findBySellerId(sellerId);
        return orders.stream()
                .map(this::mapToOrderResponseDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    // Get orders by buyer ID
    public List<Order> getOrdersByBuyer(UUID buyerId) {
        return orderRepository.findByBuyerId(buyerId);
    }

    // Get orders by seller ID
    public List<Order> getOrdersBySeller(UUID sellerId) {
        return orderRepository.findBySellerId(sellerId);
    }

    // Delete all orders (Admin functionality)
    public void deleteAllOrders() {
        orderRepository.deleteAll();
    }

    // Add delivery files
    public Order addDeliveryFiles(UUID orderId, List<MultipartFile> files) {
        Order order = getOrderById(orderId);

        // Only seller can add delivery files
        // Additional validation can be added here based on authentication context

        // Upload files and get URLs
        List<String> fileUrls = fileUploadService.uploadFiles(files);

        // Store file URLs in the order (assuming comma-separated format)
        String existingFiles = order.getDeliveredFiles();
        String newFiles = String.join(",", fileUrls);

        if (existingFiles != null && !existingFiles.isEmpty()) {
            order.setDeliveredFiles(existingFiles + "," + newFiles);
        } else {
            order.setDeliveredFiles(newFiles);
        }

        // Update status to delivered if not already
        if (order.getStatus() != Order.OrderStatus.DELIVERED && order.getStatus() != Order.OrderStatus.COMPLETED) {
            order.setStatus(Order.OrderStatus.DELIVERED);
        }

        return orderRepository.save(order);
    }

    // Update order requirements
    public Order updateRequirements(UUID orderId, UpdateRequirementsRequest request) {
        Order order = getOrderById(orderId);

        // Only buyer can update requirements and only if order is in early stages
        if (order.getStatus() == Order.OrderStatus.DELIVERED || order.getStatus() == Order.OrderStatus.COMPLETED) {
            throw new RuntimeException("Cannot update requirements for orders that are delivered or completed");
        }

        order.setRequirements(request.getRequirements());
        return orderRepository.save(order);
    }

    // Cancel order
    public Order cancelOrder(UUID orderId) {
        Order order = getOrderById(orderId);

        // Check if order can be cancelled
        if (order.getStatus() == Order.OrderStatus.DELIVERED || order.getStatus() == Order.OrderStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel orders that are delivered or completed");
        }

        order.setStatus(Order.OrderStatus.CANCELED);
        return orderRepository.save(order);
    }

    // Validate status transitions
    private void validateStatusTransition(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        // Add business logic for valid status transitions
        // For example: NEW -> IN_PROGRESS -> DELIVERED -> COMPLETED
        // or NEW -> CANCELED, IN_PROGRESS -> CANCELED (but not DELIVERED -> CANCELED)

        if (currentStatus == Order.OrderStatus.COMPLETED) {
            throw new RuntimeException("Cannot change status of completed orders");
        }

        if (currentStatus == Order.OrderStatus.CANCELED) {
            throw new RuntimeException("Cannot change status of canceled orders");
        }
    }

    private OrderResponseDTO mapToOrderResponseDTO(Order order) {
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

        // Fetch user names from auth service
        try {
            UserProfileDTO buyerProfile = authServiceClient.getUserProfile(order.getBuyerId());
            response.setBuyerName(buyerProfile.getName());
        } catch (Exception e) {
            System.err.println("Failed to fetch buyer name for ID: " + order.getBuyerId() + " - " + e.getMessage());
            response.setBuyerName("Unknown");
        }

        try {
            UserProfileDTO sellerProfile = authServiceClient.getUserProfile(order.getSellerId());
            response.setSellerName(sellerProfile.getName());
        } catch (Exception e) {
            System.err.println("Failed to fetch seller name for ID: " + order.getSellerId() + " - " + e.getMessage());
            response.setSellerName("Unknown");
        }

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

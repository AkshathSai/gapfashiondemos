package com.gap.ecommerceapp.service;

import com.gap.ecommerceapp.dto.*;
import com.gap.ecommerceapp.exception.InsufficientStockException;
import com.gap.ecommerceapp.exception.ResourceNotFoundException;
import com.gap.ecommerceapp.model.*;
import com.gap.ecommerceapp.repository.*;
import com.gap.ecommerceapp.client.BankServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final UserService userService;
    private final BankServiceClient bankServiceClient;

    // E-commerce company bank account for receiving payments
    private static final String GAP_ECOMMERCE_BANK_ACCOUNT = "1349885778";

    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {
        User user = userService.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        List<CartItem> cartItems = cartItemRepository.findByUserId(request.getUserId());
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // Validate stock availability
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName() +
                    ". Available: " + product.getStockQuantity() + ", Requested: " + cartItem.getQuantity());
            }
        }

        // Calculate total amount
        BigDecimal totalAmount = cartItems.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setTotalAmount(totalAmount);
        order.setStatus(Order.OrderStatus.PENDING);
        order = orderRepository.save(order);

        // Create order items and update stock
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItemRepository.save(orderItem);

            // Update product stock
            Product product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productService.saveProduct(product);
        }

        // Process payment
        String transactionId = processPayment(order.getId(), request.getBankAccountNumber(), totalAmount);

        if (transactionId != null && !transactionId.startsWith("ERROR")) {
            order.setStatus(Order.OrderStatus.CONFIRMED);
            order.setPaymentTransactionId(transactionId);

            // Clear cart after successful order
            cartItemRepository.deleteByUserId(request.getUserId());
        } else {
            order.setStatus(Order.OrderStatus.PAYMENT_FAILED);

            // Restore stock if payment failed
            for (CartItem cartItem : cartItems) {
                Product product = cartItem.getProduct();
                product.setStockQuantity(product.getStockQuantity() + cartItem.getQuantity());
                productService.saveProduct(product);
            }
        }

        order = orderRepository.save(order);
        return convertToOrderResponse(order);
    }

    @Transactional
    public OrderResponse buyNow(BuyNowRequest request) {
        User user = userService.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Product product = productService.getProductById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock. Available: " + product.getStockQuantity() + ", Requested: " + request.getQuantity());
        }

        BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setTotalAmount(totalAmount);
        order.setStatus(Order.OrderStatus.PENDING);
        order = orderRepository.save(order);

        // Create order item
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(request.getQuantity());
        orderItem.setUnitPrice(product.getPrice());
        orderItemRepository.save(orderItem);

        // Update product stock
        product.setStockQuantity(product.getStockQuantity() - request.getQuantity());
        productService.saveProduct(product);

        // Process payment
        String transactionId = processPayment(order.getId(), request.getBankAccountNumber(), totalAmount);

        if (transactionId != null && !transactionId.startsWith("ERROR")) {
            order.setStatus(Order.OrderStatus.CONFIRMED);
            order.setPaymentTransactionId(transactionId);
        } else {
            order.setStatus(Order.OrderStatus.PAYMENT_FAILED);

            // Restore stock if payment failed
            product.setStockQuantity(product.getStockQuantity() + request.getQuantity());
            productService.saveProduct(product);
        }

        order = orderRepository.save(order);
        return convertToOrderResponse(order);
    }

    public List<OrderResponse> getUserOrderResponses(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderResponseById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return convertToOrderResponse(order);
    }

    private String processPayment(Long orderId, String bankAccountNumber, BigDecimal amount) {
        try {
            TransferRequest transferRequest = new TransferRequest();
            transferRequest.setFromAccountNumber(bankAccountNumber);
            transferRequest.setToAccountNumber(GAP_ECOMMERCE_BANK_ACCOUNT);
            transferRequest.setAmount(amount);

            ResponseEntity<Transaction> response = bankServiceClient.transferFunds(transferRequest);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Transaction transaction = response.getBody();
                log.info("Payment processed successfully for order: {}, transaction: {}", orderId, transaction.getId());
                return transaction.getId().toString();
            } else {
                log.error("Payment failed for order: {}, HTTP status: {}", orderId, response.getStatusCode());
                return "ERROR: Payment processing failed";
            }
        } catch (Exception e) {
            log.error("Payment failed for order: {}, error: {}", orderId, e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private OrderResponse convertToOrderResponse(Order order) {
        // Fetch order items explicitly
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());

        List<OrderItemResponse> orderItemResponses = orderItems.stream()
                .map(this::convertToOrderItemResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser().getId())
                .userName(order.getUser().getName())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().toString())
                .paymentTransactionId(order.getPaymentTransactionId())
                .createdAt(order.getCreatedAt())
                .orderItems(orderItemResponses)
                .build();
    }

    private OrderItemResponse convertToOrderItemResponse(OrderItem orderItem) {
        return OrderItemResponse.builder()
                .orderItemId(orderItem.getId())
                .productId(orderItem.getProduct().getId())
                .productName(orderItem.getProduct().getName())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .totalPrice(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .build();
    }
}

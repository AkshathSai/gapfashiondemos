package com.gap.ecommerceapp.service;

import com.gap.ecommerceapp.client.BankServiceClient;
import com.gap.ecommerceapp.dto.OrderResult;
import com.gap.ecommerceapp.dto.Transaction;
import com.gap.ecommerceapp.dto.TransferRequest;
import com.gap.ecommerceapp.model.*;
import com.gap.ecommerceapp.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductService productService;
    private final BankServiceClient bankServiceClient;

    // E-commerce company bank account for receiving payments
    private static final String GAP_ECOMMERCE_BANK_ACCOUNT = "1349885778";
    private final UserService userService;

    @Transactional
    public OrderResult createOrder(User user, String bankAccountNumber) {
        List<CartItem> cartItems = cartService.getCartItems(user.getId());
        if (cartItems.isEmpty()) {
            return OrderResult.error("Cart is empty");
        }

        // Calculate total amount
        BigDecimal totalAmount = cartService.calculateCartTotal(user.getId());

        // Ensure user has a bank account
        OrderResult bankAccountValidation = ensureUserHasBankAccount(user, bankAccountNumber);
        if (!bankAccountValidation.isSuccess()) {
            return bankAccountValidation;
        }

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setTotalAmount(totalAmount);
        order.setStatus(Order.OrderStatus.PENDING);

        // Create order items
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItems.add(orderItem);
        }

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Process payment
        String paymentResult = processPayment(savedOrder);

        if (paymentResult == null) { // null indicates success
            // Update product stock
            for (OrderItem orderItem : orderItems) {
                productService.updateStock(orderItem.getProduct().getId(), orderItem.getQuantity());
            }

            // Clear cart
            cartService.clearCart(user.getId());

            savedOrder.setStatus(Order.OrderStatus.CONFIRMED);
            Order finalOrder = orderRepository.save(savedOrder);
            return OrderResult.success(finalOrder);
        } else {
            savedOrder.setStatus(Order.OrderStatus.PAYMENT_FAILED);
            orderRepository.save(savedOrder);
            return OrderResult.error("Payment failed: " + paymentResult);
        }
    }

    private OrderResult ensureUserHasBankAccount(User user, String providedAccountNumber) {
        // Check if user already has a bank account
        if (user.getBankAccountNumber() != null && !user.getBankAccountNumber().trim().isEmpty()) {
            return OrderResult.success(null);
        }

        // If no account in DB, check if one was provided
        if (providedAccountNumber == null || providedAccountNumber.trim().isEmpty()) {
            return OrderResult.error("User must have a bank account number to place an order");
        }

        // Save the provided account number
        user.setBankAccountNumber(providedAccountNumber.trim());
        userService.updateUser(user);

        return OrderResult.success(null);
    }

    private String processPayment(Order order) {
        try {
            TransferRequest transferRequest = new TransferRequest();
            transferRequest.setFromAccountNumber(order.getUser().getBankAccountNumber());
            transferRequest.setToAccountNumber(GAP_ECOMMERCE_BANK_ACCOUNT);
            transferRequest.setAmount(order.getTotalAmount());

            ResponseEntity<Transaction> response = bankServiceClient.transferFunds(transferRequest);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Transaction transaction = response.getBody();
                order.setPaymentTransactionId(transaction.getId().toString());
                log.info("Payment processed successfully for order: {}, transaction: {}",
                        order.getOrderNumber(), transaction.getId());
                return null; // null indicates success
            } else {
                return "Bank service returned error response";
            }
        } catch (Exception e) {
            log.error("Payment failed for order: {}, error: {}", order.getOrderNumber(), e.getMessage());
            return e.getMessage();
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public Optional<Order> getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    public List<Order> getOrdersByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findOrdersByUserAndDateRange(userId, startDate, endDate);
    }
}

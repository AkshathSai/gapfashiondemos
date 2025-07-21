package com.gap.ecommerceapp.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class DTOTest {

    @Test
    void testAddToCartRequest() {
        // Given
        AddToCartRequest request = new AddToCartRequest();
        request.setUserId(1L);
        request.setProductId(2L);
        request.setQuantity(3);

        // Then
        assertEquals(1L, request.getUserId());
        assertEquals(2L, request.getProductId());
        assertEquals(3, request.getQuantity());
    }

    @Test
    void testBuyNowRequest() {
        // Given
        BuyNowRequest request = new BuyNowRequest();
        request.setUserId(1L);
        request.setProductId(2L);
        request.setQuantity(3);
        request.setBankAccountNumber("1234567890");

        // Then
        assertEquals(1L, request.getUserId());
        assertEquals(2L, request.getProductId());
        assertEquals(3, request.getQuantity());
        assertEquals("1234567890", request.getBankAccountNumber());
    }

    @Test
    void testCheckoutRequest() {
        // Given
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId(1L);
        request.setBankAccountNumber("1234567890");

        // Then
        assertEquals(1L, request.getUserId());
        assertEquals("1234567890", request.getBankAccountNumber());
    }

    @Test
    void testLoginRequest() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // Then
        assertEquals("test@example.com", request.getEmail());
        assertEquals("password123", request.getPassword());
    }

    @Test
    void testTransferRequest() {
        // Given
        TransferRequest request = new TransferRequest();
        request.setFromAccountNumber("1234567890");
        request.setToAccountNumber("0987654321");
        request.setAmount(new BigDecimal("100.00"));

        // Then
        assertEquals("1234567890", request.getFromAccountNumber());
        assertEquals("0987654321", request.getToAccountNumber());
        assertEquals(new BigDecimal("100.00"), request.getAmount());
    }

    @Test
    void testTransaction() {
        // Given
        Transaction transaction = new Transaction();
        transaction.setId(123L);
        transaction.setFromAccountNumber("1234567890");
        transaction.setToAccountNumber("0987654321");
        transaction.setAmount(new BigDecimal("100.00"));

        // Then
        assertEquals(123L, transaction.getId());
        assertEquals("1234567890", transaction.getFromAccountNumber());
        assertEquals("0987654321", transaction.getToAccountNumber());
        assertEquals(new BigDecimal("100.00"), transaction.getAmount());
    }

    @Test
    void testCartItemResponseBuilder() {
        // Given & When
        CartItemResponse response = CartItemResponse.builder()
                .cartItemId(1L)
                .productId(2L)
                .productName("Test Product")
                .quantity(3)
                .unitPrice(new BigDecimal("29.99"))
                .totalPrice(new BigDecimal("89.97"))
                .build();

        // Then
        assertEquals(1L, response.getCartItemId());
        assertEquals(2L, response.getProductId());
        assertEquals("Test Product", response.getProductName());
        assertEquals(3, response.getQuantity());
        assertEquals(new BigDecimal("29.99"), response.getUnitPrice());
        assertEquals(new BigDecimal("89.97"), response.getTotalPrice());
    }

    @Test
    void testCartResponseBuilder() {
        // Given
        CartItemResponse item = CartItemResponse.builder()
                .cartItemId(1L)
                .productId(1L)
                .productName("Test Product")
                .quantity(2)
                .unitPrice(new BigDecimal("29.99"))
                .totalPrice(new BigDecimal("59.98"))
                .build();

        // When
        CartResponse response = CartResponse.builder()
                .userId(1L)
                .items(Arrays.asList(item))
                .totalAmount(new BigDecimal("59.98"))
                .totalItems(2)
                .build();

        // Then
        assertEquals(1L, response.getUserId());
        assertEquals(1, response.getItems().size());
        assertEquals(new BigDecimal("59.98"), response.getTotalAmount());
        assertEquals(2, response.getTotalItems());
    }

    @Test
    void testOrderItemResponseBuilder() {
        // Given & When
        OrderItemResponse response = OrderItemResponse.builder()
                .orderItemId(1L)
                .productId(2L)
                .productName("Test Product")
                .quantity(3)
                .unitPrice(new BigDecimal("29.99"))
                .totalPrice(new BigDecimal("89.97"))
                .build();

        // Then
        assertEquals(1L, response.getOrderItemId());
        assertEquals(2L, response.getProductId());
        assertEquals("Test Product", response.getProductName());
        assertEquals(3, response.getQuantity());
        assertEquals(new BigDecimal("29.99"), response.getUnitPrice());
        assertEquals(new BigDecimal("89.97"), response.getTotalPrice());
    }

    @Test
    void testOrderResponseBuilder() {
        // Given
        OrderItemResponse orderItem = OrderItemResponse.builder()
                .orderItemId(1L)
                .productId(1L)
                .productName("Test Product")
                .quantity(2)
                .unitPrice(new BigDecimal("29.99"))
                .totalPrice(new BigDecimal("59.98"))
                .build();

        // When
        OrderResponse response = OrderResponse.builder()
                .orderId(1L)
                .orderNumber("ORD-123456")
                .userId(1L)
                .userName("John Doe")
                .totalAmount(new BigDecimal("59.98"))
                .status("CONFIRMED")
                .paymentTransactionId("TXN-123")
                .createdAt(LocalDateTime.now())
                .orderItems(Arrays.asList(orderItem))
                .build();

        // Then
        assertEquals(1L, response.getOrderId());
        assertEquals("ORD-123456", response.getOrderNumber());
        assertEquals(1L, response.getUserId());
        assertEquals("John Doe", response.getUserName());
        assertEquals(new BigDecimal("59.98"), response.getTotalAmount());
        assertEquals("CONFIRMED", response.getStatus());
        assertEquals("TXN-123", response.getPaymentTransactionId());
        assertNotNull(response.getCreatedAt());
        assertEquals(1, response.getOrderItems().size());
    }
}

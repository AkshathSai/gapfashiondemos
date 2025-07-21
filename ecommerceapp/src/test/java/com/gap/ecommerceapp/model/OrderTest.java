package com.gap.ecommerceapp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Order order;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John Doe");

        order = new Order();
    }

    @Test
    void testOrderCreationAndGetters() {
        // Given
        order.setId(1L);
        order.setUser(user);
        order.setOrderNumber("ORD-123456");
        order.setTotalAmount(new BigDecimal("99.99"));
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentTransactionId("TXN-123");
        order.setCreatedAt(java.time.LocalDateTime.now()); // Set createdAt manually for testing

        // Then
        assertEquals(1L, order.getId());
        assertEquals(user, order.getUser());
        assertEquals("ORD-123456", order.getOrderNumber());
        assertEquals(new BigDecimal("99.99"), order.getTotalAmount());
        assertEquals(Order.OrderStatus.PENDING, order.getStatus());
        assertEquals("TXN-123", order.getPaymentTransactionId());
        assertNotNull(order.getCreatedAt());
    }

    @Test
    void testOrderStatusEnum() {
        // Test all order status values
        assertEquals("PENDING", Order.OrderStatus.PENDING.toString());
        assertEquals("CONFIRMED", Order.OrderStatus.CONFIRMED.toString());
        assertEquals("PAYMENT_FAILED", Order.OrderStatus.PAYMENT_FAILED.toString());
        assertEquals("SHIPPED", Order.OrderStatus.SHIPPED.toString());
        assertEquals("DELIVERED", Order.OrderStatus.DELIVERED.toString());
        assertEquals("CANCELLED", Order.OrderStatus.CANCELLED.toString());
    }
}

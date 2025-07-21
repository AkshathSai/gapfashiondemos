package com.gap.ecommerceapp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    private OrderItem orderItem;
    private Order order;
    private Product product;
    private User user;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");

        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setOrderNumber("ORD-123456");

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("29.99"));

        orderItem = new OrderItem();
    }

    @Test
    void testOrderItemCreationAndGetters() {
        // Given
        orderItem.setId(1L);
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(new BigDecimal("29.99"));

        // Then
        assertEquals(1L, orderItem.getId());
        assertEquals(order, orderItem.getOrder());
        assertEquals(product, orderItem.getProduct());
        assertEquals(2, orderItem.getQuantity());
        assertEquals(new BigDecimal("29.99"), orderItem.getUnitPrice());
    }

    @Test
    void testOrderItemToString() {
        // Given
        orderItem.setId(1L);
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(new BigDecimal("29.99"));

        // When
        String toString = orderItem.toString();

        // Then
        assertNotNull(toString);
    }
}

package com.gap.ecommerceapp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CartItemTest {

    private CartItem cartItem;
    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("29.99"));

        cartItem = new CartItem();
    }

    @Test
    void testCartItemCreationAndGetters() {
        // Given
        cartItem.setId(1L);
        cartItem.setCart(new Cart());
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(new BigDecimal("29.99"));

        // Then
        assertEquals(1L, cartItem.getId());
        assertNotNull(cartItem.getCart());
        assertEquals(product, cartItem.getProduct());
        assertEquals(2, cartItem.getQuantity());
        assertEquals(new BigDecimal("29.99"), cartItem.getUnitPrice());
    }

    @Test
    void testGetTotalPrice() {
        // Given
        cartItem.setQuantity(3);
        cartItem.setUnitPrice(new BigDecimal("25.00"));

        // When
        BigDecimal totalPrice = cartItem.getTotalPrice();

        // Then
        assertEquals(new BigDecimal("75.00"), totalPrice);
    }

    @Test
    void testCartItemToString() {
        // Given
        cartItem.setId(1L);
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(new BigDecimal("29.99"));

        // When
        String toString = cartItem.toString();

        // Then
        assertNotNull(toString);
    }
}

package com.gap.ecommerceapp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
    }

    @Test
    void testProductCreationAndGetters() {
        // Given
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(new BigDecimal("29.99"));
        product.setCategory("Electronics");
        product.setStockQuantity(10);

        // Then
        assertEquals(1L, product.getId());
        assertEquals("Test Product", product.getName());
        assertEquals("Test Description", product.getDescription());
        assertEquals(new BigDecimal("29.99"), product.getPrice());
        assertEquals("Electronics", product.getCategory());
        assertEquals(10, product.getStockQuantity());
    }

    @Test
    void testProductPriceValidation() {
        // Given
        product.setPrice(new BigDecimal("0.00"));

        // Then
        assertEquals(new BigDecimal("0.00"), product.getPrice());
    }

    @Test
    void testProductStockQuantityValidation() {
        // Given
        product.setStockQuantity(0);

        // Then
        assertEquals(0, product.getStockQuantity());
    }
}

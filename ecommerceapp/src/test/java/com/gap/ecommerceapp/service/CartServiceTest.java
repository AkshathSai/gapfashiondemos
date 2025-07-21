package com.gap.ecommerceapp.service;

import com.gap.ecommerceapp.dto.CartResponse;
import com.gap.ecommerceapp.dto.CartItemResponse;
import com.gap.ecommerceapp.exception.InsufficientStockException;
import com.gap.ecommerceapp.exception.ResourceNotFoundException;
import com.gap.ecommerceapp.model.*;
import com.gap.ecommerceapp.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CartService cartService;

    private Product testProduct;
    private CartItem testCartItem;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("29.99"));
        testProduct.setStockQuantity(10);

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUser(testUser);

        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setCart(testCart);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
        testCartItem.setUnitPrice(new BigDecimal("29.99"));
    }

    @Test
    void getCartByUserId_Success() {
        // Given
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(testCartItem));

        // When
        CartResponse result = cartService.getCartByUserId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(2, result.getTotalItems());
        assertEquals(new BigDecimal("59.98"), result.getTotalAmount());
        assertEquals(1, result.getItems().size());
    }

    @Test
    void getCartByUserId_EmptyCart() {
        // Given
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of());

        // When
        CartResponse result = cartService.getCartByUserId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(0, result.getTotalItems());
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void addToCart_NewItem_Success() {
        // Given
        User user = new User();
        user.setId(1L);

        when(productService.getProductById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // When
        cartService.addToCart(user, 1L, 2);

        // Then
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addToCart_ExistingItem_UpdateQuantity() {
        // Given
        User user = new User();
        user.setId(1L);

        // Create existing item with proper initialization
        CartItem existingItem = new CartItem();
        existingItem.setId(1L);
        existingItem.setCart(testCart);
        existingItem.setProduct(testProduct);
        existingItem.setQuantity(1);
        existingItem.setUnitPrice(testProduct.getPrice()); // Use the same price as testProduct

        when(productService.getProductById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.of(existingItem));

        // Mock the save to return the same existing item (which will be modified by the service)
        when(cartItemRepository.save(existingItem)).thenReturn(existingItem);

        // When
        CartItemResponse result = cartService.addToCart(user, 1L, 2);

        // Then
        assertNotNull(result);
        verify(cartItemRepository).save(existingItem);
        assertEquals(3, existingItem.getQuantity()); // 1 + 2
        assertEquals(testProduct.getPrice(), existingItem.getUnitPrice()); // Should be updated
    }

    @Test
    void addToCart_ProductNotFound_ThrowsException() {
        // Given
        User user = new User();
        user.setId(1L);

        when(productService.getProductById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> cartService.addToCart(user, 1L, 2));
    }

    @Test
    void addToCart_InsufficientStock_ThrowsException() {
        // Given
        User user = new User();
        user.setId(1L);

        testProduct.setStockQuantity(1); // Less than requested quantity
        when(productService.getProductById(1L)).thenReturn(Optional.of(testProduct));

        // When & Then
        assertThrows(InsufficientStockException.class, () -> cartService.addToCart(user, 1L, 2));
    }

    @Test
    void updateCartItemQuantity_Success() {
        // Given
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // When
        cartService.updateCartItemQuantity(1L, 5);

        // Then
        verify(cartItemRepository).save(testCartItem);
        assertEquals(5, testCartItem.getQuantity());
    }

    @Test
    void updateCartItemQuantity_ItemNotFound_ThrowsException() {
        // Given
        when(cartItemRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> cartService.updateCartItemQuantity(1L, 5));
    }

    @Test
    void updateCartItemQuantity_InsufficientStock_ThrowsException() {
        // Given
        testProduct.setStockQuantity(3); // Less than requested quantity
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));

        // When & Then
        assertThrows(InsufficientStockException.class, () -> cartService.updateCartItemQuantity(1L, 5));
    }

    @Test
    void removeFromCart_Success() {
        // Given
        when(cartItemRepository.existsById(1L)).thenReturn(true);

        // When
        cartService.removeFromCart(1L);

        // Then
        verify(cartItemRepository).deleteById(1L);
    }

    @Test
    void removeFromCart_ItemNotFound_ThrowsException() {
        // Given
        when(cartItemRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> cartService.removeFromCart(1L));
    }

    @Test
    void clearCart_Success() {
        // When
        cartService.clearCart(1L);

        // Then
        verify(cartItemRepository).deleteByUserId(1L);
    }
}

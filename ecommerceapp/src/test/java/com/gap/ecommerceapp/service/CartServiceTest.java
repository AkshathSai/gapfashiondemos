package com.gap.ecommerceapp.service;

import com.gap.ecommerceapp.model.CartItem;
import com.gap.ecommerceapp.model.Product;
import com.gap.ecommerceapp.model.User;
import com.gap.ecommerceapp.repository.CartItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Product testProduct;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStockQuantity(10);

        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setUser(testUser);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
        testCartItem.setUnitPrice(new BigDecimal("99.99"));
    }

    @Test
    void getCartItems_ShouldReturnCartItems() {
        // Arrange
        Long userId = 1L;
        List<CartItem> expectedItems = Arrays.asList(testCartItem);
        when(cartItemRepository.findByUserId(userId)).thenReturn(expectedItems);

        // Act
        List<CartItem> result = cartService.getCartItems(userId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(expectedItems, result);
        verify(cartItemRepository, times(1)).findByUserId(userId);
    }

    @Test
    void getCartItems_ShouldReturnEmptyList_WhenNoItems() {
        // Arrange
        Long userId = 1L;
        when(cartItemRepository.findByUserId(userId)).thenReturn(Arrays.asList());

        // Act
        List<CartItem> result = cartService.getCartItems(userId);

        // Assert
        assertTrue(result.isEmpty());
        verify(cartItemRepository, times(1)).findByUserId(userId);
    }

    @Test
    void addToCart_ShouldAddNewItem_WhenProductNotInCart() {
        // Arrange
        Long productId = 1L;
        Integer quantity = 3;
        when(productService.getProductById(productId)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByUserIdAndProductId(testUser.getId(), productId)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // Act
        CartItem result = cartService.addToCart(testUser, productId, quantity);

        // Assert
        assertNotNull(result);
        verify(productService, times(1)).getProductById(productId);
        verify(cartItemRepository, times(1)).findByUserIdAndProductId(testUser.getId(), productId);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void addToCart_ShouldUpdateExistingItem_WhenProductAlreadyInCart() {
        // Arrange
        Long productId = 1L;
        Integer quantity = 3;
        testCartItem.setQuantity(2); // Existing quantity
        when(productService.getProductById(productId)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByUserIdAndProductId(testUser.getId(), productId)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(testCartItem)).thenReturn(testCartItem);

        // Act
        CartItem result = cartService.addToCart(testUser, productId, quantity);

        // Assert
        assertNotNull(result);
        assertEquals(5, testCartItem.getQuantity()); // 2 + 3
        verify(cartItemRepository, times(1)).save(testCartItem);
    }

    @Test
    void addToCart_ShouldThrowException_WhenProductNotFound() {
        // Arrange
        Long productId = 999L;
        Integer quantity = 1;
        when(productService.getProductById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> cartService.addToCart(testUser, productId, quantity)
        );

        assertEquals("Product not found", exception.getMessage());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addToCart_ShouldThrowException_WhenInsufficientStock() {
        // Arrange
        Long productId = 1L;
        Integer quantity = 15; // More than available stock (10)
        when(productService.getProductById(productId)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> cartService.addToCart(testUser, productId, quantity)
        );

        assertEquals("Insufficient stock", exception.getMessage());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addToCart_ShouldHandleZeroQuantity() {
        // Arrange
        Long productId = 1L;
        Integer quantity = 0;
        when(productService.getProductById(productId)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByUserIdAndProductId(testUser.getId(), productId)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // Act
        CartItem result = cartService.addToCart(testUser, productId, quantity);

        // Assert
        assertNotNull(result);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void removeFromCart_ShouldDeleteCartItem() {
        // Arrange
        Long cartItemId = 1L;

        // Act
        cartService.removeFromCart(cartItemId);

        // Assert
        verify(cartItemRepository, times(1)).deleteById(cartItemId);
    }

    @Test
    void clearCart_ShouldDeleteAllUserCartItems() {
        // Arrange
        Long userId = 1L;

        // Act
        cartService.clearCart(userId);

        // Assert
        verify(cartItemRepository, times(1)).deleteByUserId(userId);
    }

    @Test
    void calculateCartTotal_ShouldReturnCorrectTotal() {
        // Arrange
        Long userId = 1L;
        CartItem item1 = new CartItem();
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("10.00"));

        CartItem item2 = new CartItem();
        item2.setQuantity(3);
        item2.setUnitPrice(new BigDecimal("15.00"));

        List<CartItem> cartItems = Arrays.asList(item1, item2);
        when(cartItemRepository.findByUserId(userId)).thenReturn(cartItems);

        // Act
        BigDecimal total = cartService.calculateCartTotal(userId);

        // Assert
        assertEquals(new BigDecimal("65.00"), total); // (2*10) + (3*15) = 20 + 45 = 65
        verify(cartItemRepository, times(1)).findByUserId(userId);
    }

    @Test
    void calculateCartTotal_ShouldReturnZero_WhenCartIsEmpty() {
        // Arrange
        Long userId = 1L;
        when(cartItemRepository.findByUserId(userId)).thenReturn(Arrays.asList());

        // Act
        BigDecimal total = cartService.calculateCartTotal(userId);

        // Assert
        assertEquals(BigDecimal.ZERO, total);
        verify(cartItemRepository, times(1)).findByUserId(userId);
    }

    @Test
    void calculateCartTotal_ShouldHandleDecimalPrices() {
        // Arrange
        Long userId = 1L;
        CartItem item = new CartItem();
        item.setQuantity(3);
        item.setUnitPrice(new BigDecimal("12.99"));

        when(cartItemRepository.findByUserId(userId)).thenReturn(Arrays.asList(item));

        // Act
        BigDecimal total = cartService.calculateCartTotal(userId);

        // Assert
        assertEquals(new BigDecimal("38.97"), total); // 3 * 12.99 = 38.97
        verify(cartItemRepository, times(1)).findByUserId(userId);
    }
}

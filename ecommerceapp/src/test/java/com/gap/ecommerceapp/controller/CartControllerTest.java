package com.gap.ecommerceapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gap.ecommerceapp.model.CartItem;
import com.gap.ecommerceapp.model.Product;
import com.gap.ecommerceapp.model.User;
import com.gap.ecommerceapp.service.CartService;
import com.gap.ecommerceapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private CartService cartService;

    @Mock
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Product testProduct;
    private CartItem testCartItem;
    private CartController.AddToCartRequest addToCartRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));

        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setUser(testUser);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
        testCartItem.setUnitPrice(new BigDecimal("99.99"));

        addToCartRequest = new CartController.AddToCartRequest();
        addToCartRequest.setUserId(1L);
        addToCartRequest.setProductId(1L);
        addToCartRequest.setQuantity(2);
    }

    @Test
    void getCartItems_ShouldReturnCartItems() throws Exception {
        // Arrange
        Long userId = 1L;
        List<CartItem> cartItems = Arrays.asList(testCartItem);
        when(cartService.getCartItems(userId)).thenReturn(cartItems);

        // Act & Assert
        mockMvc.perform(get("/api/cart/user/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].quantity").value(2));

        verify(cartService, times(1)).getCartItems(userId);
    }

    @Test
    void getCartItems_ShouldReturnEmptyList_WhenNoItems() throws Exception {
        // Arrange
        Long userId = 1L;
        when(cartService.getCartItems(userId)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/cart/user/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(cartService, times(1)).getCartItems(userId);
    }

    @Test
    void addToCart_ShouldAddItemSuccessfully() throws Exception {
        // Arrange
        when(userService.findById(addToCartRequest.getUserId())).thenReturn(Optional.of(testUser));
        when(cartService.addToCart(testUser, addToCartRequest.getProductId(), addToCartRequest.getQuantity()))
                .thenReturn(testCartItem);

        // Act & Assert
        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.quantity").value(2));

        verify(userService, times(1)).findById(addToCartRequest.getUserId());
        verify(cartService, times(1)).addToCart(testUser, addToCartRequest.getProductId(), addToCartRequest.getQuantity());
    }

    @Test
    void addToCart_ShouldReturnBadRequest_WhenUserNotFound() throws Exception {
        // Arrange
        when(userService.findById(addToCartRequest.getUserId())).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).findById(addToCartRequest.getUserId());
        verify(cartService, never()).addToCart(any(User.class), anyLong(), any(Integer.class));
    }

    @Test
    void addToCart_ShouldReturnBadRequest_WhenServiceThrowsException() throws Exception {
        // Arrange
        when(userService.findById(addToCartRequest.getUserId())).thenReturn(Optional.of(testUser));
        when(cartService.addToCart(testUser, addToCartRequest.getProductId(), addToCartRequest.getQuantity()))
                .thenThrow(new IllegalArgumentException("Product not found"));

        // Act & Assert
        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isBadRequest());

        verify(cartService, times(1)).addToCart(testUser, addToCartRequest.getProductId(), addToCartRequest.getQuantity());
    }

    @Test
    void addToCart_ShouldReturnBadRequest_WhenInvalidJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"invalid\" \"json\""))
                .andExpect(status().isBadRequest());

        verify(userService, never()).findById(anyLong());
        verify(cartService, never()).addToCart(any(User.class), anyLong(), any(Integer.class));
    }

    @Test
    void addToCart_ShouldHandleZeroQuantity() throws Exception {
        // Arrange
        addToCartRequest.setQuantity(0);
        when(userService.findById(addToCartRequest.getUserId())).thenReturn(Optional.of(testUser));
        when(cartService.addToCart(testUser, addToCartRequest.getProductId(), addToCartRequest.getQuantity()))
                .thenReturn(testCartItem);

        // Act & Assert
        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isOk());

        verify(cartService, times(1)).addToCart(testUser, addToCartRequest.getProductId(), 0);
    }

    @Test
    void removeFromCart_ShouldRemoveItemSuccessfully() throws Exception {
        // Arrange
        Long cartItemId = 1L;

        // Act & Assert
        mockMvc.perform(delete("/api/cart/remove/{cartItemId}", cartItemId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(cartService, times(1)).removeFromCart(cartItemId);
    }

    @Test
    void clearCart_ShouldClearCartSuccessfully() throws Exception {
        // Arrange
        Long userId = 1L;

        // Act & Assert
        mockMvc.perform(delete("/api/cart/clear/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(cartService, times(1)).clearCart(userId);
    }

    @Test
    void getCartTotal_ShouldReturnTotal() throws Exception {
        // Arrange
        Long userId = 1L;
        BigDecimal expectedTotal = new BigDecimal("199.98");
        when(cartService.calculateCartTotal(userId)).thenReturn(expectedTotal);

        // Act & Assert
        mockMvc.perform(get("/api/cart/total/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("199.98"));

        verify(cartService, times(1)).calculateCartTotal(userId);
    }

    @Test
    void getCartTotal_ShouldReturnZero_WhenCartIsEmpty() throws Exception {
        // Arrange
        Long userId = 1L;
        when(cartService.calculateCartTotal(userId)).thenReturn(BigDecimal.ZERO);

        // Act & Assert
        mockMvc.perform(get("/api/cart/total/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));

        verify(cartService, times(1)).calculateCartTotal(userId);
    }

    @Test
    void addToCart_ShouldHandleNullUserId() throws Exception {
        // Arrange
        addToCartRequest.setUserId(null);

        // Act & Assert
        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).findById(anyLong());
    }

    @Test
    void addToCart_ShouldHandleNullProductId() throws Exception {
        // Arrange
        addToCartRequest.setProductId(null);
        when(userService.findById(addToCartRequest.getUserId())).thenReturn(Optional.of(testUser));
        when(cartService.addToCart(testUser, null, addToCartRequest.getQuantity()))
                .thenThrow(new IllegalArgumentException("Product ID cannot be null"));

        // Act & Assert
        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addToCart_ShouldHandleNullQuantity() throws Exception {
        // Arrange
        addToCartRequest.setQuantity(null);
        when(userService.findById(addToCartRequest.getUserId())).thenReturn(Optional.of(testUser));
        when(cartService.addToCart(testUser, addToCartRequest.getProductId(), null))
                .thenThrow(new IllegalArgumentException("Quantity cannot be null"));

        // Act & Assert
        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isBadRequest());
    }
}

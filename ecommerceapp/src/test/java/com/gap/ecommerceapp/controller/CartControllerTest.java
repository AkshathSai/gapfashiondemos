package com.gap.ecommerceapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gap.ecommerceapp.dto.*;
import com.gap.ecommerceapp.exception.ResourceNotFoundException;
import com.gap.ecommerceapp.model.User;
import com.gap.ecommerceapp.service.CartService;
import com.gap.ecommerceapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private AddToCartRequest addToCartRequest;
    private CartResponse cartResponse;
    private CartItemResponse cartItemResponse;
    private User mockUser;

    @BeforeEach
    void setUp() {
        addToCartRequest = new AddToCartRequest();
        addToCartRequest.setUserId(1L);
        addToCartRequest.setProductId(1L);
        addToCartRequest.setQuantity(2);

        cartItemResponse = CartItemResponse.builder()
                .cartItemId(1L)
                .productId(1L)
                .productName("Test Product")
                .quantity(2)
                .unitPrice(new BigDecimal("29.99"))
                .totalPrice(new BigDecimal("59.98"))
                .build();

        cartResponse = CartResponse.builder()
                .userId(1L)
                .items(List.of(cartItemResponse))
                .totalAmount(new BigDecimal("59.98"))
                .totalItems(2)
                .build();

        mockUser = new User();
        mockUser.setId(1L);
    }

    @Test
    void addToCart_Success() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(mockUser));
        when(cartService.addToCart(any(User.class), anyLong(), anyInt())).thenReturn(cartItemResponse);

        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cartItemId").value(1L))
                .andExpect(jsonPath("$.productId").value(1L));

        verify(userService).findById(1L);
        verify(cartService).addToCart(any(User.class), eq(1L), eq(2));
    }

    @Test
    void addToCart_UserNotFound_ThrowsException() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isNotFound());

        verify(userService).findById(1L);
        verify(cartService, never()).addToCart(any(User.class), anyLong(), anyInt());
    }

    @Test
    void addToCart_ProductNotFound_ThrowsException() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(mockUser));
        when(cartService.addToCart(any(User.class), anyLong(), anyInt()))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addToCartRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCart_Success() throws Exception {
        when(cartService.getCartByUserId(1L)).thenReturn(cartResponse);

        mockMvc.perform(get("/api/cart/user/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.totalAmount").value(59.98))
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].productId").value(1L));

        verify(cartService).getCartByUserId(1L);
    }

    @Test
    void updateCartItem_Success() throws Exception {
        when(cartService.updateCartItemQuantity(1L, 3)).thenReturn(cartItemResponse);

        mockMvc.perform(put("/api/cart/item/1")
                .param("quantity", "3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cartItemId").value(1L));

        verify(cartService).updateCartItemQuantity(1L, 3);
    }

    @Test
    void updateCartItem_ItemNotFound_ThrowsException() throws Exception {
        when(cartService.updateCartItemQuantity(anyLong(), anyInt()))
                .thenThrow(new ResourceNotFoundException("Cart item not found"));

        mockMvc.perform(put("/api/cart/item/1")
                .param("quantity", "3"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeCartItem_Success() throws Exception {
        doNothing().when(cartService).removeFromCart(anyLong());

        mockMvc.perform(delete("/api/cart/item/1"))
                .andExpect(status().isOk());

        verify(cartService).removeFromCart(1L);
    }

    @Test
    void removeCartItem_ItemNotFound_ThrowsException() throws Exception {
        doThrow(new ResourceNotFoundException("Cart item not found"))
                .when(cartService).removeFromCart(anyLong());

        mockMvc.perform(delete("/api/cart/item/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void clearCart_Success() throws Exception {
        doNothing().when(cartService).clearCart(anyLong());

        mockMvc.perform(delete("/api/cart/clear/1"))
                .andExpect(status().isOk());

        verify(cartService).clearCart(1L);
    }
}

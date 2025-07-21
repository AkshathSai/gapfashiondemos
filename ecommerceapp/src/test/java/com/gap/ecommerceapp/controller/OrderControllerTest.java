package com.gap.ecommerceapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gap.ecommerceapp.dto.*;
import com.gap.ecommerceapp.exception.InsufficientStockException;
import com.gap.ecommerceapp.exception.ResourceNotFoundException;
import com.gap.ecommerceapp.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private CheckoutRequest checkoutRequest;
    private BuyNowRequest buyNowRequest;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        checkoutRequest = new CheckoutRequest();
        checkoutRequest.setUserId(1L);
        checkoutRequest.setBankAccountNumber("1234567890");

        buyNowRequest = new BuyNowRequest();
        buyNowRequest.setUserId(1L);
        buyNowRequest.setProductId(1L);
        buyNowRequest.setQuantity(2);
        buyNowRequest.setBankAccountNumber("1234567890");

        OrderItemResponse orderItem = OrderItemResponse.builder()
                .orderItemId(1L)
                .productId(1L)
                .productName("Test Product")
                .quantity(2)
                .unitPrice(new BigDecimal("29.99"))
                .totalPrice(new BigDecimal("59.98"))
                .build();

        orderResponse = OrderResponse.builder()
                .orderId(1L)
                .orderNumber("ORD-123456")
                .userId(1L)
                .userName("John Doe")
                .totalAmount(new BigDecimal("59.98"))
                .status("CONFIRMED")
                .paymentTransactionId("TXN-123")
                .createdAt(LocalDateTime.now())
                .orderItems(List.of(orderItem))
                .build();
    }

    @Test
    void checkout_Success() throws Exception {
        when(orderService.checkout(any(CheckoutRequest.class))).thenReturn(orderResponse);

        mockMvc.perform(post("/api/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.orderNumber").value("ORD-123456"))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.totalAmount").value(59.98))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(orderService).checkout(any(CheckoutRequest.class));
    }

    @Test
    void checkout_InsufficientStock_ThrowsException() throws Exception {
        when(orderService.checkout(any(CheckoutRequest.class)))
                .thenThrow(new InsufficientStockException("Insufficient stock"));

        mockMvc.perform(post("/api/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkout_UserNotFound_ThrowsException() throws Exception {
        when(orderService.checkout(any(CheckoutRequest.class)))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(post("/api/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void buyNow_Success() throws Exception {
        when(orderService.buyNow(any(BuyNowRequest.class))).thenReturn(orderResponse);

        mockMvc.perform(post("/api/orders/buy-now")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buyNowRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.orderNumber").value("ORD-123456"))
                .andExpect(jsonPath("$.totalAmount").value(59.98));

        verify(orderService).buyNow(any(BuyNowRequest.class));
    }

    @Test
    void buyNow_InsufficientStock_ThrowsException() throws Exception {
        when(orderService.buyNow(any(BuyNowRequest.class)))
                .thenThrow(new InsufficientStockException("Insufficient stock"));

        mockMvc.perform(post("/api/orders/buy-now")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buyNowRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserOrders_Success() throws Exception {
        List<OrderResponse> orders = List.of(orderResponse);
        when(orderService.getUserOrderResponses(1L)).thenReturn(orders);

        mockMvc.perform(get("/api/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].orderId").value(1L))
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-123456"));

        verify(orderService).getUserOrderResponses(1L);
    }

    @Test
    void getUserOrders_EmptyList() throws Exception {
        when(orderService.getUserOrderResponses(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(orderService).getUserOrderResponses(1L);
    }
}

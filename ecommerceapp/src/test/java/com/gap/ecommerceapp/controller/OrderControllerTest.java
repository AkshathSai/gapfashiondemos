package com.gap.ecommerceapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gap.ecommerceapp.model.Order;
import com.gap.ecommerceapp.model.User;
import com.gap.ecommerceapp.service.OrderService;
import com.gap.ecommerceapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @Mock
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setBankAccountNumber("1234567890");

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setOrderNumber("ORD-123456789");
        testOrder.setTotalAmount(new BigDecimal("199.98"));
        testOrder.setStatus(Order.OrderStatus.CONFIRMED);
        testOrder.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void purchaseProducts_ShouldCreateOrderSuccessfully() throws Exception {
        // Arrange
        Long userId = 1L;
        when(userService.findById(userId)).thenReturn(Optional.of(testUser));
        when(orderService.createOrder(testUser)).thenReturn(testOrder);

        // Act & Assert
        mockMvc.perform(post("/api/orders/purchase/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderNumber").value("ORD-123456789"))
                .andExpect(jsonPath("$.totalAmount").value(199.98))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(userService, times(1)).findById(userId);
        verify(orderService, times(1)).createOrder(testUser);
    }

    @Test
    void purchaseProducts_ShouldReturnBadRequest_WhenUserNotFound() throws Exception {
        // Arrange
        Long userId = 999L;
        when(userService.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/orders/purchase/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).findById(userId);
        verify(orderService, never()).createOrder(any(User.class));
    }

    @Test
    void purchaseProducts_ShouldReturnBadRequest_WhenServiceThrowsException() throws Exception {
        // Arrange
        Long userId = 1L;
        when(userService.findById(userId)).thenReturn(Optional.of(testUser));
        when(orderService.createOrder(testUser)).thenThrow(new IllegalArgumentException("Cart is empty"));

        // Act & Assert
        mockMvc.perform(post("/api/orders/purchase/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderService, times(1)).createOrder(testUser);
    }

    @Test
    void getUserOrders_ShouldReturnUserOrders() throws Exception {
        // Arrange
        Long userId = 1L;
        List<Order> orders = Arrays.asList(testOrder);
        when(orderService.getUserOrders(userId)).thenReturn(orders);

        // Act & Assert
        mockMvc.perform(get("/api/orders/user/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-123456789"));

        verify(orderService, times(1)).getUserOrders(userId);
    }

    @Test
    void getUserOrders_ShouldReturnEmptyList_WhenNoOrders() throws Exception {
        // Arrange
        Long userId = 1L;
        when(orderService.getUserOrders(userId)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/orders/user/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(orderService, times(1)).getUserOrders(userId);
    }

    @Test
    void getOrderByNumber_ShouldReturnOrder_WhenOrderExists() throws Exception {
        // Arrange
        String orderNumber = "ORD-123456789";
        when(orderService.getOrderByNumber(orderNumber)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        mockMvc.perform(get("/api/orders/number/{orderNumber}", orderNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderNumber").value("ORD-123456789"))
                .andExpect(jsonPath("$.totalAmount").value(199.98));

        verify(orderService, times(1)).getOrderByNumber(orderNumber);
    }

    @Test
    void getOrderByNumber_ShouldReturnNotFound_WhenOrderNotExists() throws Exception {
        // Arrange
        String orderNumber = "ORD-999999999";
        when(orderService.getOrderByNumber(orderNumber)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/orders/number/{orderNumber}", orderNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).getOrderByNumber(orderNumber);
    }

    @Test
    void getDashboardData_ShouldReturnOrdersForYearMonth() throws Exception {
        // Arrange
        Long userId = 1L;
        int year = 2024;
        int month = 12;
        List<Order> orders = Arrays.asList(testOrder);
        when(orderService.getOrdersByDateRange(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(orders);

        // Act & Assert
        mockMvc.perform(get("/api/orders/dashboard/{userId}", userId)
                .param("year", String.valueOf(year))
                .param("month", String.valueOf(month))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(orderService, times(1)).getOrdersByDateRange(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getDashboardData_ShouldReturnEmptyList_WhenNoOrdersInPeriod() throws Exception {
        // Arrange
        Long userId = 1L;
        int year = 2024;
        int month = 1;
        when(orderService.getOrdersByDateRange(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/orders/dashboard/{userId}", userId)
                .param("year", String.valueOf(year))
                .param("month", String.valueOf(month))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(orderService, times(1)).getOrdersByDateRange(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getDashboardDataByRange_ShouldReturnOrdersForDateRange() throws Exception {
        // Arrange
        Long userId = 1L;
        String startDate = "2024-12-01T00:00:00";
        String endDate = "2024-12-31T23:59:59";
        List<Order> orders = Arrays.asList(testOrder);
        when(orderService.getOrdersByDateRange(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(orders);

        // Act & Assert
        mockMvc.perform(get("/api/orders/dashboard/{userId}/range", userId)
                .param("startDate", startDate)
                .param("endDate", endDate)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(orderService, times(1)).getOrdersByDateRange(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getDashboardDataByRange_ShouldReturnBadRequest_WhenInvalidDateFormat() throws Exception {
        // Arrange
        Long userId = 1L;
        String startDate = "invalid-date";
        String endDate = "2024-12-31T23:59:59";

        // Act & Assert
        mockMvc.perform(get("/api/orders/dashboard/{userId}/range", userId)
                .param("startDate", startDate)
                .param("endDate", endDate)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).getOrdersByDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getDashboardDataByRange_ShouldReturnBadRequest_WhenMissingStartDate() throws Exception {
        // Arrange
        Long userId = 1L;
        String endDate = "2024-12-31T23:59:59";

        // Act & Assert
        mockMvc.perform(get("/api/orders/dashboard/{userId}/range", userId)
                .param("endDate", endDate)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).getOrdersByDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getDashboardDataByRange_ShouldReturnBadRequest_WhenMissingEndDate() throws Exception {
        // Arrange
        Long userId = 1L;
        String startDate = "2024-12-01T00:00:00";

        // Act & Assert
        mockMvc.perform(get("/api/orders/dashboard/{userId}/range", userId)
                .param("startDate", startDate)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).getOrdersByDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void purchaseProducts_ShouldHandleInsufficientBalanceException() throws Exception {
        // Arrange
        Long userId = 1L;
        when(userService.findById(userId)).thenReturn(Optional.of(testUser));
        when(orderService.createOrder(testUser))
                .thenThrow(new IllegalArgumentException("Insufficient balance in bank account"));

        // Act & Assert
        mockMvc.perform(post("/api/orders/purchase/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderService, times(1)).createOrder(testUser);
    }

    @Test
    void purchaseProducts_ShouldHandleBankAccountNotFoundException() throws Exception {
        // Arrange
        Long userId = 1L;
        when(userService.findById(userId)).thenReturn(Optional.of(testUser));
        when(orderService.createOrder(testUser))
                .thenThrow(new IllegalArgumentException("User must have a bank account number to place an order"));

        // Act & Assert
        mockMvc.perform(post("/api/orders/purchase/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderService, times(1)).createOrder(testUser);
    }

    @Test
    void getDashboardData_ShouldHandleInvalidYearMonth() throws Exception {
        // Arrange
        Long userId = 1L;

        // Act & Assert
        mockMvc.perform(get("/api/orders/dashboard/{userId}", userId)
                .param("year", "invalid")
                .param("month", "invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).getOrdersByDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getDashboardData_ShouldHandleBoundaryMonths() throws Exception {
        // Arrange
        Long userId = 1L;
        int year = 2024;
        int month = 1; // January
        when(orderService.getOrdersByDateRange(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/orders/dashboard/{userId}", userId)
                .param("year", String.valueOf(year))
                .param("month", String.valueOf(month))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(orderService, times(1)).getOrdersByDateRange(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getDashboardData_ShouldHandleDecemberMonth() throws Exception {
        // Arrange
        Long userId = 1L;
        int year = 2024;
        int month = 12; // December
        when(orderService.getOrdersByDateRange(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testOrder));

        // Act & Assert
        mockMvc.perform(get("/api/orders/dashboard/{userId}", userId)
                .param("year", String.valueOf(year))
                .param("month", String.valueOf(month))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(orderService, times(1)).getOrdersByDateRange(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class));
    }
}

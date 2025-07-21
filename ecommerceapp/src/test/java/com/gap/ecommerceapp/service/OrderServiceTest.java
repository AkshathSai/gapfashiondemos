package com.gap.ecommerceapp.service;

import com.gap.ecommerceapp.client.BankServiceClient;
import com.gap.ecommerceapp.dto.*;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductService productService;

    @Mock
    private UserService userService;

    @Mock
    private BankServiceClient bankServiceClient;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Product testProduct;
    private CartItem testCartItem;
    private Order testOrder;
    private OrderItem testOrderItem;
    private CheckoutRequest checkoutRequest;
    private BuyNowRequest buyNowRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@example.com");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("29.99"));
        testProduct.setStockQuantity(10);

        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
        testCartItem.setUnitPrice(new BigDecimal("29.99"));

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setOrderNumber("ORD-123456");
        testOrder.setTotalAmount(new BigDecimal("59.98"));
        testOrder.setStatus(Order.OrderStatus.CONFIRMED);
        testOrder.setCreatedAt(LocalDateTime.now());

        testOrderItem = new OrderItem();
        testOrderItem.setId(1L);
        testOrderItem.setOrder(testOrder);
        testOrderItem.setProduct(testProduct);
        testOrderItem.setQuantity(2);
        testOrderItem.setUnitPrice(new BigDecimal("29.99"));

        checkoutRequest = new CheckoutRequest();
        checkoutRequest.setUserId(1L);
        checkoutRequest.setBankAccountNumber("1234567890");

        buyNowRequest = new BuyNowRequest();
        buyNowRequest.setUserId(1L);
        buyNowRequest.setProductId(1L);
        buyNowRequest.setQuantity(2);
        buyNowRequest.setBankAccountNumber("1234567890");
    }

    @Test
    void checkout_UserNotFound_ThrowsException() {
        // Given
        when(userService.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> orderService.checkout(checkoutRequest));
    }

    @Test
    void checkout_EmptyCart_ThrowsException() {
        // Given
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartItemRepository.findByUserId(1L)).thenReturn(Arrays.asList());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> orderService.checkout(checkoutRequest));
    }

    @Test
    void checkout_InsufficientStock_ThrowsException() {
        // Given
        testProduct.setStockQuantity(1); // Less than cart item quantity (2)
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartItemRepository.findByUserId(1L)).thenReturn(Arrays.asList(testCartItem));

        // When & Then
        assertThrows(InsufficientStockException.class, () -> orderService.checkout(checkoutRequest));
    }

    @Test
    void buyNow_UserNotFound_ThrowsException() {
        // Given
        when(userService.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> orderService.buyNow(buyNowRequest));
    }

    @Test
    void buyNow_ProductNotFound_ThrowsException() {
        // Given
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(productService.getProductById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> orderService.buyNow(buyNowRequest));
    }

    @Test
    void buyNow_InsufficientStock_ThrowsException() {
        // Given
        testProduct.setStockQuantity(1); // Less than requested quantity (2)
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(productService.getProductById(1L)).thenReturn(Optional.of(testProduct));

        // When & Then
        assertThrows(InsufficientStockException.class, () -> orderService.buyNow(buyNowRequest));
    }

    @Test
    void getUserOrderResponses_Success() {
        // Given
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));

        // When
        List<OrderResponse> result = orderService.getUserOrderResponses(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getOrderId());
    }

    @Test
    void getOrderResponseById_Success() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));

        // When
        OrderResponse result = orderService.getOrderResponseById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getOrderId());
    }

    @Test
    void getOrderResponseById_OrderNotFound_ThrowsException() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderResponseById(1L));
    }
}

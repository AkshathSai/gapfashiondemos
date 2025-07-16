package com.gap.ecommerceapp.service;

import com.gap.ecommerceapp.client.BankServiceClient;
import com.gap.ecommerceapp.dto.Account;
import com.gap.ecommerceapp.dto.Transaction;
import com.gap.ecommerceapp.dto.TransferRequest;
import com.gap.ecommerceapp.model.*;
import com.gap.ecommerceapp.repository.OrderRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @Mock
    private ProductService productService;

    @Mock
    private BankServiceClient bankServiceClient;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Product testProduct;
    private CartItem testCartItem;
    private Order testOrder;
    private Account testAccount;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setBankAccountNumber("1234567890");

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

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setOrderNumber("ORD-123456789");
        testOrder.setTotalAmount(new BigDecimal("199.98"));
        testOrder.setStatus(Order.OrderStatus.PENDING);

        testAccount = new Account();
        testAccount.setAccountNumber("1234567890");
        testAccount.setBalance(new BigDecimal("500.00"));

        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setFromAccountNumber("1234567890");
        testTransaction.setToAccountNumber("1349885778");
        testTransaction.setAmount(new BigDecimal("199.98"));
    }

    @Test
    void createOrder_ShouldCreateOrderSuccessfully_WhenValidRequest() {
        // Arrange
        List<CartItem> cartItems = Arrays.asList(testCartItem);
        when(cartService.getCartItems(testUser.getId())).thenReturn(cartItems);
        when(cartService.calculateCartTotal(testUser.getId())).thenReturn(new BigDecimal("199.98"));
        when(bankServiceClient.getAccountByNumber(testUser.getBankAccountNumber()))
                .thenReturn(new ResponseEntity<>(testAccount, HttpStatus.OK));
        when(bankServiceClient.transferFunds(any(TransferRequest.class)))
                .thenReturn(new ResponseEntity<>(testTransaction, HttpStatus.OK));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(productService.updateStock(anyLong(), anyInt())).thenReturn(true);

        // Act
        Order result = orderService.createOrder(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(testOrder, result);
        verify(cartService, times(1)).getCartItems(testUser.getId());
        verify(cartService, times(1)).calculateCartTotal(testUser.getId());
        verify(bankServiceClient, times(1)).getAccountByNumber(testUser.getBankAccountNumber());
        verify(bankServiceClient, times(1)).transferFunds(any(TransferRequest.class));
        verify(productService, times(1)).updateStock(testProduct.getId(), testCartItem.getQuantity());
        verify(cartService, times(1)).clearCart(testUser.getId());
        verify(orderRepository, times(2)).save(any(Order.class)); // Once before payment, once after
    }

    @Test
    void createOrder_ShouldThrowException_WhenCartIsEmpty() {
        // Arrange
        when(cartService.getCartItems(testUser.getId())).thenReturn(Arrays.asList());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> orderService.createOrder(testUser)
        );

        assertEquals("Cart is empty", exception.getMessage());
        verify(cartService, times(1)).getCartItems(testUser.getId());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_ShouldThrowException_WhenUserHasNoBankAccount() {
        // Arrange
        testUser.setBankAccountNumber(null);
        List<CartItem> cartItems = Arrays.asList(testCartItem);
        when(cartService.getCartItems(testUser.getId())).thenReturn(cartItems);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> orderService.createOrder(testUser)
        );

        assertEquals("User must have a bank account number to place an order", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_ShouldThrowException_WhenUserHasEmptyBankAccount() {
        // Arrange
        testUser.setBankAccountNumber("   ");
        List<CartItem> cartItems = Arrays.asList(testCartItem);
        when(cartService.getCartItems(testUser.getId())).thenReturn(cartItems);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> orderService.createOrder(testUser)
        );

        assertEquals("User must have a bank account number to place an order", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_ShouldThrowException_WhenBankAccountNotFound() {
        // Arrange
        List<CartItem> cartItems = Arrays.asList(testCartItem);
        when(cartService.getCartItems(testUser.getId())).thenReturn(cartItems);
        when(cartService.calculateCartTotal(testUser.getId())).thenReturn(new BigDecimal("199.98"));
        when(bankServiceClient.getAccountByNumber(testUser.getBankAccountNumber()))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> orderService.createOrder(testUser)
        );

        assertEquals("Invalid bank account number", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_ShouldThrowException_WhenInsufficientBalance() {
        // Arrange
        testAccount.setBalance(new BigDecimal("100.00")); // Less than order total
        List<CartItem> cartItems = Arrays.asList(testCartItem);
        when(cartService.getCartItems(testUser.getId())).thenReturn(cartItems);
        when(cartService.calculateCartTotal(testUser.getId())).thenReturn(new BigDecimal("199.98"));
        when(bankServiceClient.getAccountByNumber(testUser.getBankAccountNumber()))
                .thenReturn(new ResponseEntity<>(testAccount, HttpStatus.OK));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> orderService.createOrder(testUser)
        );

        assertEquals("Insufficient balance in bank account", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_ShouldHandlePaymentFailure() {
        // Arrange
        List<CartItem> cartItems = Arrays.asList(testCartItem);
        when(cartService.getCartItems(testUser.getId())).thenReturn(cartItems);
        when(cartService.calculateCartTotal(testUser.getId())).thenReturn(new BigDecimal("199.98"));
        when(bankServiceClient.getAccountByNumber(testUser.getBankAccountNumber()))
                .thenReturn(new ResponseEntity<>(testAccount, HttpStatus.OK));
        when(bankServiceClient.transferFunds(any(TransferRequest.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.BAD_REQUEST));

        Order savedOrder = new Order();
        savedOrder.setStatus(Order.OrderStatus.PAYMENT_FAILED);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        Order result = orderService.createOrder(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(Order.OrderStatus.PAYMENT_FAILED, result.getStatus());
        verify(productService, never()).updateStock(anyLong(), anyInt());
        verify(cartService, never()).clearCart(anyLong());
    }

    @Test
    void createOrder_ShouldHandleBankServiceException() {
        // Arrange
        List<CartItem> cartItems = Arrays.asList(testCartItem);
        when(cartService.getCartItems(testUser.getId())).thenReturn(cartItems);
        when(cartService.calculateCartTotal(testUser.getId())).thenReturn(new BigDecimal("199.98"));
        when(bankServiceClient.getAccountByNumber(testUser.getBankAccountNumber()))
                .thenThrow(new RuntimeException("Bank service unavailable"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> orderService.createOrder(testUser)
        );

        assertTrue(exception.getMessage().contains("Error verifying bank account"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getUserOrders_ShouldReturnUserOrders() {
        // Arrange
        Long userId = 1L;
        List<Order> expectedOrders = Arrays.asList(testOrder);
        when(orderRepository.findByUserId(userId)).thenReturn(expectedOrders);

        // Act
        List<Order> result = orderService.getUserOrders(userId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(expectedOrders, result);
        verify(orderRepository, times(1)).findByUserId(userId);
    }

    @Test
    void getUserOrders_ShouldReturnEmptyList_WhenNoOrders() {
        // Arrange
        Long userId = 1L;
        when(orderRepository.findByUserId(userId)).thenReturn(Arrays.asList());

        // Act
        List<Order> result = orderService.getUserOrders(userId);

        // Assert
        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findByUserId(userId);
    }

    @Test
    void getOrderByNumber_ShouldReturnOrder_WhenOrderExists() {
        // Arrange
        String orderNumber = "ORD-123456789";
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(testOrder));

        // Act
        Optional<Order> result = orderService.getOrderByNumber(orderNumber);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testOrder, result.get());
        verify(orderRepository, times(1)).findByOrderNumber(orderNumber);
    }

    @Test
    void getOrderByNumber_ShouldReturnEmpty_WhenOrderNotExists() {
        // Arrange
        String orderNumber = "ORD-999999999";
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.empty());

        // Act
        Optional<Order> result = orderService.getOrderByNumber(orderNumber);

        // Assert
        assertFalse(result.isPresent());
        verify(orderRepository, times(1)).findByOrderNumber(orderNumber);
    }

    @Test
    void getOrdersByDateRange_ShouldReturnOrdersInRange() {
        // Arrange
        Long userId = 1L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        List<Order> expectedOrders = Arrays.asList(testOrder);
        when(orderRepository.findOrdersByUserAndDateRange(userId, startDate, endDate)).thenReturn(expectedOrders);

        // Act
        List<Order> result = orderService.getOrdersByDateRange(userId, startDate, endDate);

        // Assert
        assertEquals(1, result.size());
        assertEquals(expectedOrders, result);
        verify(orderRepository, times(1)).findOrdersByUserAndDateRange(userId, startDate, endDate);
    }

    @Test
    void getOrdersByDateRange_ShouldReturnEmptyList_WhenNoOrdersInRange() {
        // Arrange
        Long userId = 1L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now().minusDays(15);
        when(orderRepository.findOrdersByUserAndDateRange(userId, startDate, endDate)).thenReturn(Arrays.asList());

        // Act
        List<Order> result = orderService.getOrdersByDateRange(userId, startDate, endDate);

        // Assert
        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findOrdersByUserAndDateRange(userId, startDate, endDate);
    }

    @Test
    void createOrder_ShouldGenerateUniqueOrderNumber() {
        // Arrange
        List<CartItem> cartItems = Arrays.asList(testCartItem);
        when(cartService.getCartItems(testUser.getId())).thenReturn(cartItems);
        when(cartService.calculateCartTotal(testUser.getId())).thenReturn(new BigDecimal("199.98"));
        when(bankServiceClient.getAccountByNumber(testUser.getBankAccountNumber()))
                .thenReturn(new ResponseEntity<>(testAccount, HttpStatus.OK));
        when(bankServiceClient.transferFunds(any(TransferRequest.class)))
                .thenReturn(new ResponseEntity<>(testTransaction, HttpStatus.OK));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            return order;
        });
        when(productService.updateStock(anyLong(), anyInt())).thenReturn(true);

        // Act
        Order result = orderService.createOrder(testUser);

        // Assert
        assertNotNull(result.getOrderNumber());
        assertTrue(result.getOrderNumber().startsWith("ORD-"));
        assertTrue(result.getOrderNumber().length() > 10);
    }
}

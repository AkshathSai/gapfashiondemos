package com.gap.ecommerceapp.service;

import com.gap.ecommerceapp.client.BankServiceClient;
import com.gap.ecommerceapp.dto.Account;
import com.gap.ecommerceapp.dto.OrderResult;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
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

    @Mock
    private UserService userService;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Product testProduct;
    private CartItem testCartItem;
    private Order testOrder;
    private Account testAccount;
    private Transaction testTransaction;
    private String testBankAccountNumber;

    @BeforeEach
    void setUp() {
        testBankAccountNumber = "1234567890";

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setBankAccountNumber(testBankAccountNumber);

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
        testAccount.setAccountNumber(testBankAccountNumber);
        testAccount.setBalance(new BigDecimal("500.00"));

        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setFromAccountNumber(testBankAccountNumber);
        testTransaction.setToAccountNumber("1349885778");
        testTransaction.setAmount(new BigDecimal("199.98"));
    }

    @Test
    void createOrder_ShouldCreateOrderSuccessfully_WhenValidRequest() {
        // Arrange
        List<CartItem> cartItems = List.of(testCartItem);
        when(cartService.getCartItems(testUser.getId())).thenReturn(cartItems);
        when(cartService.calculateCartTotal(testUser.getId())).thenReturn(new BigDecimal("199.98"));
        when(bankServiceClient.getAccountByNumber(testUser.getBankAccountNumber()))
                .thenReturn(new ResponseEntity<>(testAccount, HttpStatus.OK));
        when(bankServiceClient.transferFunds(any(TransferRequest.class)))
                .thenReturn(new ResponseEntity<>(testTransaction, HttpStatus.OK));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(productService.updateStock(anyLong(), anyInt())).thenReturn(true);

        // Act
        OrderResult result = orderService.createOrder(testUser, testBankAccountNumber);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getOrder());
        assertNull(result.getErrorMessage());
        verify(cartService, times(1)).getCartItems(testUser.getId());
        verify(cartService, times(1)).calculateCartTotal(testUser.getId());
        verify(bankServiceClient, times(1)).getAccountByNumber(testUser.getBankAccountNumber());
        verify(bankServiceClient, times(1)).transferFunds(any(TransferRequest.class));
        verify(productService, times(1)).updateStock(testProduct.getId(), testCartItem.getQuantity());
        verify(cartService, times(1)).clearCart(testUser.getId());
        verify(orderRepository, times(2)).save(any(Order.class)); // Once before payment, once after
    }

    @Test
    void createOrder_ShouldReturnError_WhenCartIsEmpty() {
        // Arrange
        when(cartService.getCartItems(testUser.getId())).thenReturn(Collections.emptyList());

        // Act
        OrderResult result = orderService.createOrder(testUser, testBankAccountNumber);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Cart is empty", result.getErrorMessage());
        assertNull(result.getOrder());
        verify(cartService, times(1)).getCartItems(testUser.getId());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_ShouldReturnError_WhenUserHasNoBankAccount() {
        // Arrange
        testUser.setBankAccountNumber(null);
        List<CartItem> cartItems = List.of(testCartItem);
        when(cartService.getCartItems(testUser.getId())).thenReturn(cartItems);

        // Act
        OrderResult result = orderService.createOrder(testUser, null);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("User must have a bank account number to place an order", result.getErrorMessage());
        assertNull(result.getOrder());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_ShouldReturnError_WhenUserHasEmptyBankAccount() {
        // Arrange
        testUser.setBankAccountNumber("   ");
        List<CartItem> cartItems = List.of(testCartItem);
        when(cartService.getCartItems(testUser.getId())).thenReturn(cartItems);

        // Act
        OrderResult result = orderService.createOrder(testUser, null);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("User must have a bank account number to place an order", result.getErrorMessage());
        assertNull(result.getOrder());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_ShouldReturnError_WhenBankAccountNotFound() {
        // Arrange
        List<CartItem> cartItems = List.of(testCartItem);
        when(cartService.getCartItems(testUser.getId())).thenReturn(cartItems);
        when(cartService.calculateCartTotal(testUser.getId())).thenReturn(new BigDecimal("199.98"));
        when(bankServiceClient.getAccountByNumber(testUser.getBankAccountNumber()))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));

        // Act
        OrderResult result = orderService.createOrder(testUser, testBankAccountNumber);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Invalid bank account number", result.getErrorMessage());
        assertNull(result.getOrder());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_ShouldReturnError_WhenInsufficientBalance() {
        // Arrange
        testAccount.setBalance(new BigDecimal("100.00")); // Less than order total
        List<CartItem> cartItems = List.of(testCartItem);
        when(cartService.getCartItems(testUser.getId())).thenReturn(cartItems);
        when(cartService.calculateCartTotal(testUser.getId())).thenReturn(new BigDecimal("199.98"));
        when(bankServiceClient.getAccountByNumber(testUser.getBankAccountNumber()))
                .thenReturn(new ResponseEntity<>(testAccount, HttpStatus.OK));

        // Act
        OrderResult result = orderService.createOrder(testUser, testBankAccountNumber);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Insufficient balance in bank account"));
        assertTrue(result.getErrorMessage().contains("Required: $199.98"));
        assertTrue(result.getErrorMessage().contains("Available: $100.00"));
        assertNull(result.getOrder());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_ShouldReturnError_WhenBankServiceThrowsException() {
        // Arrange
        List<CartItem> cartItems = List.of(testCartItem);
        when(cartService.getCartItems(testUser.getId())).thenReturn(cartItems);
        when(cartService.calculateCartTotal(testUser.getId())).thenReturn(new BigDecimal("199.98"));
        when(bankServiceClient.getAccountByNumber(testUser.getBankAccountNumber()))
                .thenThrow(new RuntimeException("Bank service unavailable"));

        // Act
        OrderResult result = orderService.createOrder(testUser, testBankAccountNumber);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Error verifying bank account"));
        assertTrue(result.getErrorMessage().contains("Bank service unavailable"));
        assertNull(result.getOrder());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_ShouldReturnError_WhenPaymentFails() {
        // Arrange
        List<CartItem> cartItems = List.of(testCartItem);
        when(cartService.getCartItems(testUser.getId())).thenReturn(cartItems);
        when(cartService.calculateCartTotal(testUser.getId())).thenReturn(new BigDecimal("199.98"));
        when(bankServiceClient.getAccountByNumber(testUser.getBankAccountNumber()))
                .thenReturn(new ResponseEntity<>(testAccount, HttpStatus.OK));
        when(bankServiceClient.transferFunds(any(TransferRequest.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.BAD_REQUEST));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderResult result = orderService.createOrder(testUser, testBankAccountNumber);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Payment failed"));
        assertNull(result.getOrder());
        verify(orderRepository, times(2)).save(any(Order.class)); // Once before payment, once after failure
    }

    @Test
    void createOrder_ShouldUpdateUserBankAccount_WhenProvidedDuringOrder() {
        // Arrange
        testUser.setBankAccountNumber(null);
        String newBankAccount = "9876543210";
        List<CartItem> cartItems = List.of(testCartItem);
        when(cartService.getCartItems(testUser.getId())).thenReturn(cartItems);
        when(cartService.calculateCartTotal(testUser.getId())).thenReturn(new BigDecimal("199.98"));
        when(bankServiceClient.getAccountByNumber(newBankAccount))
                .thenReturn(new ResponseEntity<>(testAccount, HttpStatus.OK));
        when(bankServiceClient.transferFunds(any(TransferRequest.class)))
                .thenReturn(new ResponseEntity<>(testTransaction, HttpStatus.OK));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(productService.updateStock(anyLong(), anyInt())).thenReturn(true);

        // Act
        OrderResult result = orderService.createOrder(testUser, newBankAccount);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(userService, times(1)).updateUser(testUser);
        assertEquals(newBankAccount, testUser.getBankAccountNumber());
    }

    @Test
    void getUserOrders_ShouldReturnUserOrders() {
        // Arrange
        Long userId = 1L;
        List<Order> expectedOrders = List.of(testOrder);
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
        when(orderRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

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
        List<Order> expectedOrders = List.of(testOrder);
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
        when(orderRepository.findOrdersByUserAndDateRange(userId, startDate, endDate)).thenReturn(Collections.emptyList());

        // Act
        List<Order> result = orderService.getOrdersByDateRange(userId, startDate, endDate);

        // Assert
        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findOrdersByUserAndDateRange(userId, startDate, endDate);
    }

    @Test
    void createOrder_ShouldGenerateUniqueOrderNumber() {
        // Arrange
        List<CartItem> cartItems = List.of(testCartItem);
        when(cartService.getCartItems(testUser.getId())).thenReturn(cartItems);
        when(cartService.calculateCartTotal(testUser.getId())).thenReturn(new BigDecimal("199.98"));
        when(bankServiceClient.getAccountByNumber(testUser.getBankAccountNumber()))
                .thenReturn(new ResponseEntity<>(testAccount, HttpStatus.OK));
        when(bankServiceClient.transferFunds(any(TransferRequest.class)))
                .thenReturn(new ResponseEntity<>(testTransaction, HttpStatus.OK));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productService.updateStock(anyLong(), anyInt())).thenReturn(true);

        // Act
        OrderResult result = orderService.createOrder(testUser, testBankAccountNumber);

        // Assert
        assertNotNull(result.getOrder().getOrderNumber());
        assertTrue(result.getOrder().getOrderNumber().startsWith("ORD-"));
        assertTrue(result.getOrder().getOrderNumber().length() > 10);
    }
}

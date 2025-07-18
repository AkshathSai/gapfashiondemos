package com.gap.ecommerceapp.controller;

import com.gap.ecommerceapp.client.BankServiceClient;
import com.gap.ecommerceapp.dto.Account;
import com.gap.ecommerceapp.dto.OrderResult;
import com.gap.ecommerceapp.model.*;
import com.gap.ecommerceapp.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/demo")
public class DemoController {

    private final UserService userService;
    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;
    private final BankServiceClient bankServiceClient;

    @GetMapping("/complete-flow")
    public ResponseEntity<Map<String, Object>> demonstrateCompleteFlow() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Step 1: Register a new user
            User newUser = new User();
            newUser.setUsername("demo_user_" + System.currentTimeMillis());
            newUser.setPassword("password123");
            newUser.setEmail("demo@example.com");
            newUser.setFullName("Demo User");
            newUser.setBankAccountNumber("2349885777"); // Use existing bank account

            User registeredUser = userService.registerUser(newUser);
            result.put("user_registration", "User registered: " + registeredUser.getUsername());

            // Step 2: Login
            Optional<User> loggedInUser = userService.loginUser(registeredUser.getUsername(), "password123");
            result.put("user_login", loggedInUser.isPresent() ? "Login successful" : "✗ Login failed");

            // Step 3: Search products (without login required)
            List<Product> searchResults = productService.searchProducts("jacket");
            result.put("product_search", "Found " + searchResults.size() + " products matching 'jacket'");

            // Step 4: Add products to cart
            if (!searchResults.isEmpty()) {
                CartItem cartItem = cartService.addToCart(registeredUser, searchResults.getFirst().getId(), 2);
                result.put("add_to_cart", "Added " + cartItem.getProduct().getName() + " to cart");
            }

            // Step 5: Purchase products (OpenFeign integration) - Updated to handle OrderResult
            OrderResult orderResult = orderService.createOrder(registeredUser, null);
            if (orderResult.isSuccess()) {
                Order order = orderResult.getOrder();
                result.put("purchase", "Order created: " + order.getOrderNumber() +
                          " | Status: " + order.getStatus() +
                          " | Total: $" + order.getTotalAmount());
            } else {
                result.put("purchase", "Order failed: " + orderResult.getErrorMessage());
            }

            // Step 7: Dashboard - get monthly orders
            YearMonth currentMonth = YearMonth.now();
            LocalDateTime startDate = currentMonth.atDay(1).atStartOfDay();
            LocalDateTime endDate = currentMonth.atEndOfMonth().atTime(23, 59, 59);

            List<Order> monthlyOrders = orderService.getOrdersByDateRange(registeredUser.getId(), startDate, endDate);
            result.put("dashboard", "Monthly orders retrieved: " + monthlyOrders.size() + " orders");

            result.put("status", "SUCCESS");
            result.put("message", "Complete e-commerce flow with OpenFeign bank integration demonstrated successfully!");

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.put("status", "ERROR");
            result.put("message", "Error in demo flow: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/features-overview")
    public ResponseEntity<Map<String, Object>> getFeaturesOverview() {
        Map<String, Object> features = new HashMap<>();

        // E-Commerce Features
        Map<String, String> ecommerceFeatures = new HashMap<>();
        ecommerceFeatures.put("1_registration", "User registration with bank account integration");
        ecommerceFeatures.put("2_login", "User authentication system");
        ecommerceFeatures.put("3_search", "Product search (works with/without login)");
        ecommerceFeatures.put("4_cart", "Shopping cart management");
        ecommerceFeatures.put("5_purchase", "Purchase with bank integration via OpenFeign");
        ecommerceFeatures.put("6_dashboard", "Monthly/date range order analytics");
        features.put("ecommerce_features", ecommerceFeatures);

        // Bank Integration Features
        Map<String, String> bankFeatures = new HashMap<>();
        bankFeatures.put("1_account_verification", "Verify customer bank account exists");
        bankFeatures.put("2_balance_check", "Check sufficient funds before purchase");
        bankFeatures.put("3_money_deduction", "Transfer funds from customer to e-commerce account");
        bankFeatures.put("4_transaction_tracking", "Track payment transaction IDs");
        features.put("bank_integration_features", bankFeatures);

        // OpenFeign Integration Points
        Map<String, String> feignIntegration = new HashMap<>();
        feignIntegration.put("client", "BankServiceClient with @FeignClient annotation");
        feignIntegration.put("endpoints", "GET /api/accounts/{accountNumber}, POST /api/transfers");
        feignIntegration.put("configuration", "Configured via application.properties");
        feignIntegration.put("error_handling", "Graceful handling of bank service failures");
        features.put("openfeign_integration", feignIntegration);

        return ResponseEntity.ok(features);
    }

    @GetMapping("/test-bank-connectivity")
    public ResponseEntity<Map<String, Object>> testBankConnectivity() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Test 1: Get account information
            ResponseEntity<Account> accountResponse = bankServiceClient.getAccountByNumber("CUST001");
            if (accountResponse.getStatusCode().is2xxSuccessful()) {
                result.put("account_retrieval", "✓ Successfully retrieved account CUST001");
                result.put("account_details", accountResponse.getBody());
            } else {
                result.put("account_retrieval", "✗ Failed to retrieve account");
            }

            // Test 2: Get balance
            ResponseEntity<String> balanceResponse = bankServiceClient.getBalance("CUST001");
            if (balanceResponse.getStatusCode().is2xxSuccessful()) {
                result.put("balance_check", "✓ " + balanceResponse.getBody());
            } else {
                result.put("balance_check", "✗ Failed to get balance");
            }

            result.put("connectivity_status", "SUCCESS");

        } catch (Exception e) {
            result.put("connectivity_status", "FAILED");
            result.put("error", "Bank service connectivity error: " + e.getMessage());
            result.put("note", "Make sure BankApp is running on localhost:8080");
        }

        return ResponseEntity.ok(result);
    }
}

package com.gap.ecommerceapp.controller;

import com.gap.ecommerceapp.dto.OrderResult;
import com.gap.ecommerceapp.model.Order;
import com.gap.ecommerceapp.model.User;
import com.gap.ecommerceapp.service.OrderService;
import com.gap.ecommerceapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    @PostMapping("/purchase/{userId}")
    public ResponseEntity<?> purchaseProducts(@PathVariable Long userId,
                                             @RequestParam("bankAccountNumber") String bankAccountNumber) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        OrderResult result = orderService.createOrder(userOpt.get(), bankAccountNumber);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result.getOrder());
        } else {
            return ResponseEntity.badRequest().body(result.getErrorMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable Long userId) {
        List<Order> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<Order> getOrderByNumber(@PathVariable String orderNumber) {
        Optional<Order> order = orderService.getOrderByNumber(orderNumber);
        return order.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<List<Order>> getDashboardData(
            @PathVariable Long userId,
            @RequestParam int year,
            @RequestParam int month) {

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Order> orders = orderService.getOrdersByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/dashboard/{userId}/range")
    public ResponseEntity<List<Order>> getDashboardDataByRange(
            @PathVariable Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);

            List<Order> orders = orderService.getOrdersByDateRange(userId, start, end);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

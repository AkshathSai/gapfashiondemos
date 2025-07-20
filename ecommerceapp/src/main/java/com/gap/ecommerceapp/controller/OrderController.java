package com.gap.ecommerceapp.controller;

import com.gap.ecommerceapp.dto.BuyNowRequest;
import com.gap.ecommerceapp.dto.CheckoutRequest;
import com.gap.ecommerceapp.dto.OrderResponse;
import com.gap.ecommerceapp.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        OrderResponse orderResponse = orderService.checkout(request);
        return ResponseEntity.ok(orderResponse);
    }

    @PostMapping("/buy-now")
    public ResponseEntity<OrderResponse> buyNow(@Valid @RequestBody BuyNowRequest request) {
        OrderResponse orderResponse = orderService.buyNow(request);
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getUserOrders(@PathVariable Long userId) {
        List<OrderResponse> orders = orderService.getUserOrderResponses(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        OrderResponse order = orderService.getOrderResponseById(orderId);
        return ResponseEntity.ok(order);
    }
}

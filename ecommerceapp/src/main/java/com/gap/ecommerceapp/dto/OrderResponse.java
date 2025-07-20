package com.gap.ecommerceapp.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String userName;
    private BigDecimal totalAmount;
    private String status;
    private String paymentTransactionId;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> orderItems;
}
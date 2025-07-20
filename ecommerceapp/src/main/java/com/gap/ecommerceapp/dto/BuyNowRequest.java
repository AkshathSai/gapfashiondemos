package com.gap.ecommerceapp.dto;

import lombok.Data;

@Data
public class BuyNowRequest {
    private Long userId;
    private Long productId;
    private Integer quantity;
    private String bankAccountNumber;
}
package com.gap.ecommerceapp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BuyNowRequest {
    private Long userId;
    private Long productId;
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    @NotNull(message = "Bank Account number cannot be null")
    private String bankAccountNumber;
}
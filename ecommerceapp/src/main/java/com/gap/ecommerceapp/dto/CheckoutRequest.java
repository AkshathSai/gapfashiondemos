package com.gap.ecommerceapp.dto;

import lombok.Data;

@Data
public class CheckoutRequest {
    private Long userId;
    private String bankAccountNumber;
}

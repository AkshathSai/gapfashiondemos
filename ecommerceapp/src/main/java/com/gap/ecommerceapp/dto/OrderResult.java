package com.gap.ecommerceapp.dto;

import com.gap.ecommerceapp.model.Order;
import lombok.Data;

@Data
public class OrderResult {
    private boolean success;
    private String errorMessage;
    private Order order;

    public static OrderResult success(Order order) {
        OrderResult result = new OrderResult();
        result.success = true;
        result.order = order;
        return result;
    }

    public static OrderResult error(String errorMessage) {
        OrderResult result = new OrderResult();
        result.success = false;
        result.errorMessage = errorMessage;
        return result;
    }
}

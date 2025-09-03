package com.example.vendorservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;

@Data
public class FoodItem {

    @Id
    private int id;
    @NotEmpty(message = "Name is required")
    private String name;
    @NotEmpty(message = "Vendor is required")
    private String vendor;
    @NotEmpty(message = "Description is required")
    private String description;
    @NotNull(message = "Quantity is required")
    private Integer quantity;
    @NotNull(message = "Price is required")
    private BigDecimal price;
}

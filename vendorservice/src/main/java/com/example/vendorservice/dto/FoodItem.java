package com.example.vendorservice.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;

@Data
public class FoodItem {

    @Id
    private int id;
    private String name;
    private String vendor;
    private String description;
    private Integer quantity;
    private BigDecimal price;
}

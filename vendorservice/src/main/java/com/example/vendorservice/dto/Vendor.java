package com.example.vendorservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Vendor {

    @Id
    private Integer Id;
    @NotEmpty(message = "Name cannot be empty")
    private String name;
    @NotNull(message = "Email cannot be null")
    private String email;
    @NotEmpty(message = "Phone number cannot be empty")
    private String phoneNumber;
    @NotEmpty(message = "Address is required")
    private String address;
}

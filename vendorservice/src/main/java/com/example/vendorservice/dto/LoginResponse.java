package com.example.vendorservice.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private boolean success;
    private String message;
    private Integer vendorId;
    private String vendorName;

    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public LoginResponse(boolean success, String message, Integer vendorId, String vendorName) {
        this.success = success;
        this.message = message;
        this.vendorId = vendorId;
        this.vendorName = vendorName;
    }
}

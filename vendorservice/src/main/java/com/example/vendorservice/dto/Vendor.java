package com.example.vendorservice.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Vendor {

    @Id
    private Integer Id;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
}

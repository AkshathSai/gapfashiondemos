package com.example.vendorservice.controller;

import com.example.vendorservice.dto.FoodItem;
import com.example.vendorservice.dto.LoginRequest;
import com.example.vendorservice.dto.LoginResponse;
import com.example.vendorservice.dto.Vendor;
import com.example.vendorservice.service.KafkaFoodItemProducer;
import com.example.vendorservice.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
//@RequestMapping("/api")
@RequiredArgsConstructor
public class VendorController {

    final VendorService vendorService;
    final KafkaFoodItemProducer kafkaFoodItemProducer;

    @GetMapping("/vendors")
    public ResponseEntity<List<Vendor>> getVendors() {
        return ResponseEntity.ok(vendorService.getVendors());
    }

    @GetMapping("/vendors/{id}")
    public ResponseEntity<Vendor> getVendor(@PathVariable Integer id) {
        var vendor = vendorService.getVendor(id).orElse(null);
        if (vendor == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(vendor);
    }

    @PostMapping("/vendors")
    public ResponseEntity<Vendor> addVendor(@Valid @RequestBody Vendor vendor) {
        var savedVendorDetails = vendorService.addVendor(vendor);
        return ResponseEntity.created(URI.create("/vendors/" + savedVendorDetails.getId()))
                .body(savedVendorDetails);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = vendorService.authenticateVendor(loginRequest);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/logout/{vendorId}")
    public ResponseEntity<String> logout(@PathVariable Integer vendorId) {
        vendorService.logoutVendor(vendorId);
        return ResponseEntity.ok("Vendor logged out successfully");
    }

    @PostMapping("/food")
    public ResponseEntity<String> addFoodItem(@Valid @RequestBody FoodItem foodItem, @RequestParam Integer vendorId) {
        // Check if vendor is authenticated
        if (!vendorService.isVendorAuthenticated(vendorId)) {
            return ResponseEntity.status(401).body("Vendor must be logged in to add food items");
        }

        kafkaFoodItemProducer.addFoodItem(foodItem);
        return ResponseEntity.ok("Add Food item event posted successfully");
    }

    @DeleteMapping("/food/{id}")
    public ResponseEntity<String> deleteFoodItem(@PathVariable int id, @RequestParam Integer vendorId) {
        // Check if vendor is authenticated
        if (!vendorService.isVendorAuthenticated(vendorId)) {
            return ResponseEntity.status(401).body("Vendor must be logged in to delete food items");
        }

        kafkaFoodItemProducer.deleteFoodItem(id);
        return ResponseEntity.ok("Delete Food item event posted successfully");
    }
}

package com.example.vendorservice.controller;

import com.example.vendorservice.dto.FoodItem;
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

    @PostMapping("/food")
    public ResponseEntity<String> addFoodItem(@Valid @RequestBody FoodItem foodItem) {
        kafkaFoodItemProducer.addFoodItem(foodItem);
        return ResponseEntity.ok("Add Food item event posted successfully");
    }

    @DeleteMapping("/food/{id}")
    public ResponseEntity<String> deleteFoodItem(@PathVariable int id) {
        kafkaFoodItemProducer.deleteFoodItem(id);
        return ResponseEntity.ok("Delete Food item event posted successfully");
    }
}

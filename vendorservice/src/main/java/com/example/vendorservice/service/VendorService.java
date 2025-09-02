package com.example.vendorservice.service;

import com.example.vendorservice.dto.FoodItem;
import com.example.vendorservice.dto.Vendor;
import com.example.vendorservice.repository.FoodItemRepository;
import com.example.vendorservice.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VendorService {

    final VendorRepository vendorRepository;
    final FoodItemRepository foodItemRepository;

    public List<Vendor> getVendors() {
        return vendorRepository.findAll();
    }

    public Vendor addVendor(Vendor vendor) {
        return vendorRepository.save(vendor);
    }

    public Optional<Vendor> getVendor(Integer id) {
        return vendorRepository.findById(id);
    }

    public FoodItem addFoodItem(FoodItem foodItem) {
        return foodItemRepository.save(foodItem);
    }

    public void deleteFoodItem(int id) {
        foodItemRepository.deleteById(id);
    }
}

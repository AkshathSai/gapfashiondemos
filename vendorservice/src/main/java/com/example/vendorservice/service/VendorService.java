package com.example.vendorservice.service;

import com.example.vendorservice.dto.FoodItem;
import com.example.vendorservice.dto.LoginRequest;
import com.example.vendorservice.dto.LoginResponse;
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
    final AuthenticationService authenticationService;

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

    public LoginResponse authenticateVendor(LoginRequest loginRequest) {
        Optional<Vendor> vendorOpt = vendorRepository.findByEmail(loginRequest.getEmail());

        if (vendorOpt.isEmpty()) {
            return new LoginResponse(false, "Vendor not found with email: " + loginRequest.getEmail());
        }

        Vendor vendor = vendorOpt.get();

        // Simple password validation (in production, use hashed passwords)
        if (!vendor.getPassword().equals(loginRequest.getPassword())) {
            return new LoginResponse(false, "Invalid password");
        }

        // Login the vendor
        authenticationService.loginVendor(vendor.getId());

        return new LoginResponse(true, "Login successful", vendor.getId(), vendor.getName());
    }

    public boolean isVendorAuthenticated(Integer vendorId) {
        return authenticationService.isVendorLoggedIn(vendorId);
    }

    public void logoutVendor(Integer vendorId) {
        authenticationService.logoutVendor(vendorId);
    }
}

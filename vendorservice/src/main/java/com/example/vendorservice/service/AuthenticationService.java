package com.example.vendorservice.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class AuthenticationService {

    // Simple in-memory storage for logged-in vendors
    private final Map<Integer, Boolean> loggedInVendors = new ConcurrentHashMap<>();

    public void loginVendor(Integer vendorId) {
        loggedInVendors.put(vendorId, true);
    }

    public void logoutVendor(Integer vendorId) {
        loggedInVendors.remove(vendorId);
    }

    public boolean isVendorLoggedIn(Integer vendorId) {
        return loggedInVendors.getOrDefault(vendorId, false);
    }

    public void logoutAllVendors() {
        loggedInVendors.clear();
    }
}

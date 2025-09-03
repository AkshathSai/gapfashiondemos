package com.example.vendorservice.repository;

import com.example.vendorservice.dto.Vendor;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface VendorRepository extends MongoRepository<Vendor, Integer> {
    Optional<Vendor> findByEmail(String email);
}

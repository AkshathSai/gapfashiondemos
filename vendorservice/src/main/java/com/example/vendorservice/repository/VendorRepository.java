package com.example.vendorservice.repository;

import com.example.vendorservice.dto.Vendor;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VendorRepository extends MongoRepository<Vendor, Integer> {
}

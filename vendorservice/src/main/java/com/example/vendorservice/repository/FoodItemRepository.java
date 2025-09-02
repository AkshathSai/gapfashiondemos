package com.example.vendorservice.repository;

import com.example.vendorservice.dto.FoodItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodItemRepository extends MongoRepository<FoodItem, Integer> {
}

package com.example.vendorservice.service;

import com.example.vendorservice.dto.FoodItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaFoodItemConsumer {

    final VendorService vendorService;

    @KafkaListener(topics = "add-food-item", groupId = "vendor-service-group")
    public void consumeAdd(@Payload FoodItem foodItem) {
        log.info("Consumed message: {}", foodItem);
        log.info("Added Food item: {}", vendorService.addFoodItem(foodItem));
    }

    @KafkaListener(topics = "delete-food-item", groupId = "vendor-service-group")
    public void consumeDelete(@Payload int id) {
        log.info("Consumed message: {}", id);
        vendorService.deleteFoodItem(id);
    }
}

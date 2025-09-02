package com.example.vendorservice.service;

import com.example.vendorservice.dto.FoodItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaFoodItemProducer {

    final String ADD_FOOD_ITEM_TOPIC = "add-food-item";
    final String DELETE_FOOD_ITEM_TOPIC = "delete-food-item";
    final KafkaTemplate<String, FoodItem> kafkaTemplate;
    final KafkaTemplate<String, Integer> kafkaTemplateForDelete;

    public void addFoodItem(FoodItem foodItem) {
        kafkaTemplate.send(ADD_FOOD_ITEM_TOPIC, foodItem);
        log.info("Food item {}, has been successfully sent to the topic: {}",
                foodItem,
                ADD_FOOD_ITEM_TOPIC);
    }

    public void deleteFoodItem(int id) {
        kafkaTemplateForDelete.send(DELETE_FOOD_ITEM_TOPIC, id);
        log.info("Food item {}, has been successfully sent to the topic: {}",
                id,
                DELETE_FOOD_ITEM_TOPIC);
    }
}

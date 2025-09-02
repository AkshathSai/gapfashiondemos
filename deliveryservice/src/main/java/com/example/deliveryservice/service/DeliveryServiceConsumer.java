package com.example.deliveryservice.service;

import com.example.deliveryservice.dto.OrderResponse;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class DeliveryServiceConsumer {

    @KafkaListener(topics = "orders", groupId = "order-group")
    public void listen(@Payload OrderResponse response) {
        System.out.println("Delivery Service received response: " + response);
    }
}

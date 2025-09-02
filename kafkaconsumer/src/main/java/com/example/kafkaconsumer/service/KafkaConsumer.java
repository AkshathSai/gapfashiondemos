package com.example.kafkaconsumer.service;

import com.example.kafkaconsumer.dto.User;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    @KafkaListener(topics = "test", groupId = "test-group")
    public void consumeMessages(String message) {
        System.out.println("Received message => " + message);
    }

    @KafkaListener(topics = "user", groupId = "user-group")
    public void consumeUserMessages(@Payload User user) {
        System.out.println("Received user => " + user);
    }

}

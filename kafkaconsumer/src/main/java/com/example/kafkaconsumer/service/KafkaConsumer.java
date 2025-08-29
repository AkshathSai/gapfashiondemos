package com.example.kafkaconsumer.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    @KafkaListener(topics = "test", groupId = "test-group")
    public void consumeMessages(String message) {
        System.out.println("Received message => " + message);
    }

}

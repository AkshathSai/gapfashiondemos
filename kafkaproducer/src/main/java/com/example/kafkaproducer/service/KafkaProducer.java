package com.example.kafkaproducer.service;

import com.example.kafkaproducer.dto.User;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTemplate<String, User> kafkaTemplateUser;
    private final String TOPIC_NAME= "test";
    private final String USER_TOPIC= "user";

    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate, KafkaTemplate<String, User> kafkaTemplateUser) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTemplateUser = kafkaTemplateUser;
    }

    public void sendMessage(String message) {
        kafkaTemplate.send(TOPIC_NAME, message);
        System.out.println("Message " + message +
                " has been sucessfully sent to the topic: " + TOPIC_NAME);
    }

    public void sendMessage(User user) {
        kafkaTemplateUser.send(USER_TOPIC, user);
        System.out.println("Message " + user +
                " has been sucessfully sent to the topic: " + USER_TOPIC);
    }
}

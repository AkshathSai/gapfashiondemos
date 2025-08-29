package com.example.kafkaproducer.controller;

import com.example.kafkaproducer.service.KafkaProducer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebController {

    final KafkaProducer kafkaProducer;

    public WebController(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @GetMapping("/send")
    public String index(@RequestParam("message") String message) {
        kafkaProducer.sendMessage(message);
        return "sent " + message;
    }

}

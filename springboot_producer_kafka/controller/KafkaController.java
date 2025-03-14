package com.example.springboot_producer_kafka.controller;

import com.example.springboot_producer_kafka.producer.KafkaProducer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KafkaController {

   private final KafkaProducer kafkaProducer;

    public KafkaController(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @GetMapping("/api/send")
    public String sendMessage(@RequestParam String message) {
        kafkaProducer.sendMessage(message);
        return message;
    }

}

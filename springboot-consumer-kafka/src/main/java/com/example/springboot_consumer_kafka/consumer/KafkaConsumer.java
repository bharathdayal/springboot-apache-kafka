package com.example.springboot_consumer_kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    @KafkaListener(topics = "${spring.kafka.topic}", groupId = "${spring.kafka.group-id}")
    public void consumerListner(String message) {
        System.out.println("Message received at Consumer :" +message);
    }
}

package com.example.springboot_producer_kafka.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    @Autowired
    private final KafkaTemplate<String,String> kafkaTemplate;

    @Value("${spring.kafka.producer.topic}")
    public String topic;

    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

   public void sendMessage(String message) {
        kafkaTemplate.send(topic,message);
        System.out.println("Message Sent from Producer :" +message);
   }
}

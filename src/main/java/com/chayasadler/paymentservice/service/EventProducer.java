package com.chayasadler.paymentservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EventProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public CompletableFuture<Void> publish(String topicName, String key, String payload){
        return kafkaTemplate.send(topicName, key, payload)
                .thenAccept(String -> {});
    }
}

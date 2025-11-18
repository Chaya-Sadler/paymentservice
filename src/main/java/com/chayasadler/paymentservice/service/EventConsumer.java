package com.chayasadler.paymentservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

public class EventConsumer {

    @Autowired
    PaymentService paymentService;

    @KafkaListener(topics = "inventory.events", groupId = "payment-service-group")
    public void coonsume(String payload, Acknowledgment acknowledgment) {
        try {
            //consume the orderreserved and handle payment
            paymentService.handlePayment(payload);
            acknowledgment.acknowledge();
        } catch (Exception exe) {
            System.err.println(" Failed to process OrderReserved event from order service, offset is not committed");
        }
    }
}

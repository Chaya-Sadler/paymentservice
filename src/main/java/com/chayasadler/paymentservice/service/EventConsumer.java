package com.chayasadler.paymentservice.service;

import com.chayasadler.paymentservice.util.InventoryEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

public class EventConsumer {

    @Autowired
    PaymentService paymentService;

    @Autowired
    ObjectMapper mapper;

    @KafkaListener(topics = "inventory.events", groupId = "payment-service-group")
    public void coonsume(String payload, Acknowledgment acknowledgment) {
        try {
            //consume the inventoryReserved and handle payment
            InventoryEvent inventoryEvent = mapper.readValue(payload, InventoryEvent.class);
            if(inventoryEvent.eventType().equals("InventoryReserved")) {
                paymentService.completePayment(inventoryEvent);
            }
            acknowledgment.acknowledge();
        } catch (Exception exe) {
            System.err.println(" Failed to process OrderReserved event from order service, offset is not committed");
        }
    }
}

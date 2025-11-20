package com.chayasadler.paymentservice.service;

import com.chayasadler.paymentservice.dao.IOutBoxRepository;
import com.chayasadler.paymentservice.model.OutBoxEvent;
import com.chayasadler.paymentservice.util.EventStatus;
import com.chayasadler.paymentservice.util.InventoryEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OutboxService {

    @Autowired
    IOutBoxRepository iOutBoxRepository;

    @Autowired
    ObjectMapper mapper;

    public void saveEvent(String eventType, InventoryEvent inventoryEvent, String paymentStatus) {
        String payload;
        InventoryEvent paymentEvent = new InventoryEvent(
                UUID.randomUUID(), //new unique message id for idempotency of kafka messages
                eventType,
                inventoryEvent.orderId(),
                inventoryEvent.customerId(),
                inventoryEvent.totalAmt(),
                inventoryEvent.orderItemEventList(),
                paymentStatus.equals("AUTHORIZED") ? "Completed" : "Cancelled", //order status
                paymentStatus,
                LocalDateTime.now()
        );
        String paymentPayload;
        try {
            paymentPayload = mapper.writeValueAsString(paymentEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        OutBoxEvent outBoxEvent = new OutBoxEvent();

        outBoxEvent.setEventType(eventType);
        outBoxEvent.setAggregateId(inventoryEvent.orderId().toString());
        outBoxEvent.setAggregateType("payment");
        outBoxEvent.setPayload(paymentPayload);
        outBoxEvent.setStatus(EventStatus.UNSENT.name());
        outBoxEvent.setCreatedAt(LocalDateTime.now());

        iOutBoxRepository.save(outBoxEvent);
    }
}

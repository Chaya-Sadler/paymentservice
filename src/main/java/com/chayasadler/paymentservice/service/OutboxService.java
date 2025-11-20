package com.chayasadler.paymentservice.service;

import com.chayasadler.paymentservice.dao.IEventRepository;
import com.chayasadler.paymentservice.model.OutBoxEvent;
import com.chayasadler.paymentservice.util.EventStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OutboxService {

    @Autowired
    IEventRepository iEventRepository;

    public void saveEvent(String eventType, String orderId, String payload) {

        OutBoxEvent outBoxEvent = new OutBoxEvent();

        outBoxEvent.setEventType(eventType);
        outBoxEvent.setAggregateId(orderId);
        outBoxEvent.setAggregateType("payment");
        outBoxEvent.setPayload(payload);
        outBoxEvent.setStatus(EventStatus.UNSENT.name());
        outBoxEvent.setCreatedAt(LocalDateTime.now());

        iEventRepository.save(outBoxEvent);
    }
}

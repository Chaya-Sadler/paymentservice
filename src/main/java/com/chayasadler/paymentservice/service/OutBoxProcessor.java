package com.chayasadler.paymentservice.service;

import com.chayasadler.paymentservice.dao.IOutBoxRepository;
import com.chayasadler.paymentservice.model.OutBoxEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutBoxProcessor {

    @Value("{spring.kafka.topic}")
    private String topicName;

    @Autowired
    EventProducer eventProducer;

    @Autowired
    IOutBoxRepository iOutBoxRepository;

    private final Pageable pageSize = PageRequest.of(0, 10);

    @Transactional
    @Scheduled( fixedDelay = 3000)
    public void process() {

        List<OutBoxEvent> outBoxEventList = iOutBoxRepository.findAllUnsentOutBoxEvents(pageSize);

        if(outBoxEventList.isEmpty())
            return;

        try{
            for( OutBoxEvent outBoxEvent : outBoxEventList){
                eventProducer.publish(
                        topicName,
                        outBoxEvent.getAggregateId(),
                        outBoxEvent.getPayload()
                ).join();

                outBoxEvent.setProcessedAt(LocalDateTime.now());
                outBoxEvent.setStatus("SENT");
                //jpa flushes all commits --update the processed table
            }
        } catch (Exception exe) {
            System.err.println(" Failed to publish Payment event " + exe.getMessage());
        }
    }
}

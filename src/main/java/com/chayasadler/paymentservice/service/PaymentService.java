package com.chayasadler.paymentservice.service;

import com.chayasadler.paymentservice.dao.IEventRepository;
import com.chayasadler.paymentservice.dao.IProcessedEventRepository;
import com.chayasadler.paymentservice.dao.IPaymentRepository;
import com.chayasadler.paymentservice.model.OutBoxEvent;
import com.chayasadler.paymentservice.model.Payment;
import com.chayasadler.paymentservice.model.ProcessedEvent;
import com.chayasadler.paymentservice.util.EventStatus;
import com.chayasadler.paymentservice.util.InventoryEvent;
import com.chayasadler.paymentservice.util.PaymentStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    @Value("{payment.simulate.mode}")
    private String simulateMode;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private IProcessedEventRepository iProcessedEventRepository;

    @Autowired
    private IEventRepository iEventRepository;

    @Autowired
    private IPaymentRepository iPaymentRepository;

    @Transactional
    public void handlePayment(String payload) {

        InventoryEvent inventoryEvent;

        try {
            inventoryEvent = mapper.readValue(payload, InventoryEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        //check if the incoming message was already processed - Idempotency( Consumer part)
        Optional<ProcessedEvent> findProcessedEvent = iProcessedEventRepository.findByEventId((inventoryEvent.messageId()));
        if(findProcessedEvent.isEmpty() && inventoryEvent.orderStatus().equals("InventoryReserved")){
            //handle payment
            PaymentStatus paymentStatus = switch (simulateMode) {
                case "ALWAYS_SUCCESS" -> PaymentStatus.AUTHORIZED;
                case "RANDOM" -> Math.random() < 0.7 ? PaymentStatus.AUTHORIZED : PaymentStatus.FAILED;
                default -> PaymentStatus.FAILED;
            };

            ProcessedEvent processedEvent = new ProcessedEvent();
            processedEvent.setEventId(inventoryEvent.messageId());
            processedEvent.setEventType(inventoryEvent.orderStatus());
            processedEvent.setProcessedAt(LocalDateTime.now());

            iProcessedEventRepository.save(processedEvent);

            //save payment record in payment db
            Payment payment = new Payment();
            payment.setOrderId(inventoryEvent.orderId());
            payment.setCustomerId(inventoryEvent.customerId());
            payment.setTotalAmt(inventoryEvent.totalAmt());
            payment.setProviderTransactionId("SIM-" + UUID.randomUUID());
            payment.setPaymentMethod("SIMULATION");
            payment.setPaymentStatus(paymentStatus.name());
            payment.setFailureReason(
                    paymentStatus.name().equals("FAILED") ? "SIMULATION_FAILURE" : ""
            );
            payment.setCreatedAt(LocalDateTime.now());
            iPaymentRepository.save(payment);

            //write to outbox event (producer part)
            InventoryEvent paymentEvent = new InventoryEvent(
                    UUID.randomUUID(), //new unique message id for idempotency of kafka messages
                    inventoryEvent.orderId(),
                    inventoryEvent.customerId(),
                    inventoryEvent.totalAmt(),
                    inventoryEvent.orderItemEventList(),
                    paymentStatus.name().equals("AUTHORIZED") ? "Completed" : "Cancelled",
                    paymentStatus.name(),
                    LocalDateTime.now()
            );
            String paymentPayload;
            try {
                paymentPayload = mapper.writeValueAsString(paymentEvent);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            OutBoxEvent outBoxEvent = new OutBoxEvent();
            outBoxEvent.setEventType(
                    paymentStatus.name().equals("AUTHORIZED") ? "PaymentAuthorized" : "PaymentFailed"
            );
            outBoxEvent.setAggregateId(inventoryEvent.orderId().toString());
            outBoxEvent.setAggregateType("Payment");
            outBoxEvent.setPayload(paymentPayload);
            outBoxEvent.setStatus(EventStatus.UNSENT.name());
            outBoxEvent.setCreatedAt(LocalDateTime.now());
            iEventRepository.save(outBoxEvent);

        }
    }
}

package com.chayasadler.paymentservice.dao;

import com.chayasadler.paymentservice.model.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {

    @Query( " SELECT pe FROM ProcessedEvent pe WHERE pe.eventId =:messageId")
    public Optional<ProcessedEvent> findByEventId(UUID messageId);
}

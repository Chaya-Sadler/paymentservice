package com.chayasadler.paymentservice.dao;

import com.chayasadler.paymentservice.model.OutBoxEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IOutBoxRepository extends JpaRepository<OutBoxEvent, UUID> {

    @Query( " SELECT e from OutBoxEvent e WHERE e.processedAt is null and e.status = 'UNSENT' ORDER BY e.createdAt")
    public List<OutBoxEvent> findAllUnsentOutBoxEvents(Pageable pageSize);
}

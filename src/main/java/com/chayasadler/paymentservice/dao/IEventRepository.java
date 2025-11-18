package com.chayasadler.paymentservice.dao;

import com.chayasadler.paymentservice.model.OutBoxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IEventRepository extends JpaRepository<OutBoxEvent, UUID> {
}

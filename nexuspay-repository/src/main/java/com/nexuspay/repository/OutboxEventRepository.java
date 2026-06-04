package com.nexuspay.repository;

import com.nexuspay.domain.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxEvent.EventStatus status);
    List<OutboxEvent> findByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
            OutboxEvent.EventStatus status, Instant nextRetryAt);
}

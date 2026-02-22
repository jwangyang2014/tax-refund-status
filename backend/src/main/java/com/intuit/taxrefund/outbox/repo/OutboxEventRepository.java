package com.intuit.taxrefund.outbox.repo;

import com.intuit.taxrefund.outbox.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    // Demo-simple: just read oldest unprocessed.
    // In production: use FOR UPDATE SKIP LOCKED in a native query + separate worker transaction.
    @Query("""
        select e from OutboxEvent e
        where e.processedAt is null
        order by e.createdAt asc
    """)
    List<OutboxEvent> findUnprocessed();
}
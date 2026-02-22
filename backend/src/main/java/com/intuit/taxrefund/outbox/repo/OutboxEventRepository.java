package com.intuit.taxrefund.outbox.repo;

import com.intuit.taxrefund.outbox.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("""
        select e from OutboxEvent e
        where e.processedAt is null
        order by e.createdAt asc
    """)
    List<OutboxEvent> findUnprocessed();

    @Query("""
        select e from OutboxEvent e
        where e.processedAt is null and e.attempts < :maxAttempts
        order by e.createdAt asc
    """)
    List<OutboxEvent> findUnprocessedWithAttemptsLessThan(int maxAttempts);
}
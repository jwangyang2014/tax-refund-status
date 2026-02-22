package com.intuit.taxrefund.outbox.service;

import com.intuit.taxrefund.outbox.model.OutboxEvent;
import com.intuit.taxrefund.outbox.repo.OutboxEventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxWorker {

    private final OutboxEventRepository outboxRepo;
    private final OutboxWorkerTx tx;

    public OutboxWorker(OutboxEventRepository outboxRepo, OutboxWorkerTx tx) {
        this.outboxRepo = outboxRepo;
        this.tx = tx;
    }

    @Scheduled(fixedDelayString = "PT5S")
    public void poll() {
        // Demo-simple batch. Production: use SKIP LOCKED + LIMIT.
        List<OutboxEvent> batch = outboxRepo.findUnprocessedWithAttemptsLessThan(20);
        if (batch.isEmpty()) return;

        for (OutboxEvent evt : batch) {
            // Each event handled in its own transaction
            tx.processOne(evt.getId());
        }
    }
}
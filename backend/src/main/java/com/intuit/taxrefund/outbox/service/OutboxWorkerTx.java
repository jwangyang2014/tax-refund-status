package com.intuit.taxrefund.outbox.service;

import com.intuit.taxrefund.outbox.model.OutboxEvent;
import com.intuit.taxrefund.outbox.repo.OutboxEventRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class OutboxWorkerTx {

    private final OutboxEventRepository outboxRepo;
    private final OutboxEventHandler handler;

    public OutboxWorkerTx(OutboxEventRepository outboxRepo, OutboxEventHandler handler) {
        this.outboxRepo = outboxRepo;
        this.handler = handler;
    }

    @Transactional
    public void processOne(Long outboxEventId) {
        OutboxEvent evt = outboxRepo.findById(outboxEventId).orElse(null);
        if (evt == null) return;

        // If someone else processed it already
        if (evt.getProcessedAt() != null) return;

        try {
            handler.handle(evt);
            evt.markProcessed();
        } catch (Exception e) {
            String msg = e.getMessage() == null ? e.toString() : e.getMessage();

            // Duplicate prediction insert: treat as success (idempotent)
            if (msg.contains("already exists") || msg.contains("unique constraint") || msg.contains("duplicate key")) {
                evt.bumpAttempt(msg);
                evt.markProcessed();
            } else if (msg.contains("Model not trained yet")) {
                evt.bumpAttempt(msg);
                evt.markProcessed();
            } else {
                evt.bumpAttempt(msg);
            }
        }

        outboxRepo.save(evt);
    }
}
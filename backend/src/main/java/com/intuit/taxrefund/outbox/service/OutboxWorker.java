package com.intuit.taxrefund.outbox.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.taxrefund.ai.model.RefundEtaPrediction;
import com.intuit.taxrefund.ai.repo.RefundEtaPredictionRepository;
import com.intuit.taxrefund.ml.MlEtaClient;
import com.intuit.taxrefund.outbox.model.OutboxEvent;
import com.intuit.taxrefund.outbox.repo.OutboxEventRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Component
public class OutboxWorker {

    private static final long SECONDS_IN_DAY = 86400L;

    private final OutboxEventRepository outboxRepo;
    private final RefundEtaPredictionRepository etaRepo;
    private final MlEtaClient ml;
    private final ObjectMapper om;

    public OutboxWorker(
        OutboxEventRepository outboxRepo,
        RefundEtaPredictionRepository etaRepo,
        MlEtaClient ml,
        ObjectMapper om
    ) {
        this.outboxRepo = outboxRepo;
        this.etaRepo = etaRepo;
        this.ml = ml;
        this.om = om;
    }

    @Scheduled(fixedDelayString = "PT5S")
    @Transactional
    public void poll() {
        // Demo-simple: small batch. Production: FOR UPDATE SKIP LOCKED.
        List<OutboxEvent> batch = outboxRepo.findUnprocessedWithAttemptsLessThan(20);
        if (batch.isEmpty()) return;

        for (OutboxEvent evt : batch) {
            try {
                handle(evt);
                evt.markProcessed();
            } catch (Exception e) {
                evt.bumpAttempt(e.getMessage());
            }
            outboxRepo.save(evt);
        }
    }

    private void handle(OutboxEvent evt) throws Exception {
        if (!"REFUND_STATUS_UPDATED".equals(evt.getEventType())) {
            return; // ignore unknown types (demo)
        }

        JsonNode payload = om.readTree(evt.getPayload());

        Long userId = payload.path("userId").asLong();
        int taxYear = payload.path("taxYear").asInt();
        String status = payload.path("status").asText();

        BigDecimal expectedAmount = null;
        JsonNode amt = payload.get("expectedAmount");
        if (amt != null && !amt.isNull()) {
            // expectedAmount may be numeric or string depending on JSON serialization
            expectedAmount = new BigDecimal(amt.asText());
        }

        // Call ML
        MlEtaClient.PredictResponse pred = ml.predict(userId, taxYear, status, expectedAmount);

        // Persist prediction
        Instant estimatedAvailableAt = Instant.now().plusSeconds((long) pred.etaDays() * SECONDS_IN_DAY);

        RefundEtaPrediction row = new RefundEtaPrediction(
            userId,
            taxYear,
            status,
            pred.etaDays(),
            estimatedAvailableAt,
            pred.modelName(),
            pred.modelVersion(),
            pred.featuresJson()
        );

        etaRepo.save(row);
    }
}
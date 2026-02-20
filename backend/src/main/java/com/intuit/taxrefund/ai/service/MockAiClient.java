package com.intuit.taxrefund.ai.service;

import com.intuit.taxrefund.refund.model.RefundStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MockAiClient implements AiClient {
    private static final String MODEL_NAME = "mock-eta-v1";

    @Override
    public String getModel() {
        return MODEL_NAME;
    }

    @Override
    public PredictEtaResult predictRefundEtaDays(RefundStatus status, BigDecimal expectedAmount) {
        int days = switch (status) {
            case RECEIVED -> 14;
            case PROCESSING -> 7;
            case APPROVED -> 3;
            case SENT -> 1;
            case AVAILABLE -> 0;
            case REJECTED, NOT_FOUND -> 999;
        };

        String explanation = "Mock model: ETA based on status heuristic.";

        return new PredictEtaResult(days, explanation, "mock", MODEL_NAME);
    }
}

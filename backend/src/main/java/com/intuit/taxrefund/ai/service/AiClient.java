package com.intuit.taxrefund.ai.service;

import com.intuit.taxrefund.refund.model.RefundStatus;

import java.math.BigDecimal;

public interface AiClient {
    PredictEtaResult predictRefundEtaDays(RefundStatus status, BigDecimal expectedAmount);
    String getModel();

    record PredictEtaResult(int etaDays, String explanation, String provider, String model) {}
}

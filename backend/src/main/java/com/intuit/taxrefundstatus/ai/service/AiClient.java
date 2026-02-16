package com.intuit.taxrefundstatus.ai.service;

import com.intuit.taxrefundstatus.refund.model.RefundStatus;

import java.math.BigDecimal;

public interface AiClient {
    PredictEtaResult predictRefundEtaDays(RefundStatus status, BigDecimal expectedAmount);
    String getModel();

    record PredictEtaResult(int etaDays, String explanation, String provider, String model) {}
}

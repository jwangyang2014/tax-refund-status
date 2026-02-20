package com.intuit.taxrefund.ai;

import com.intuit.taxrefund.ai.service.MockAiClient;
import com.intuit.taxrefund.refund.model.RefundStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MockAiClientTest {

  @Test
  void predict_returns0Days_whenAvailable() {
    var client = new MockAiClient();
    var res = client.predictRefundEtaDays(RefundStatus.AVAILABLE, new BigDecimal("1.00"));
    assertEquals(0, res.etaDays());
    assertEquals("mock", res.provider());
  }

  @Test
  void predict_returnsLarge_whenRejected() {
    var client = new MockAiClient();
    var res = client.predictRefundEtaDays(RefundStatus.REJECTED, new BigDecimal("1.00"));
    assertTrue(res.etaDays() >= 365);
  }
}

package com.intuit.taxrefund.assistant.core;

import org.springframework.stereotype.Component;

@Component
public class IntentClassifier {
    public AssistantIntent classify(String text) {
        String t = text == null ? "" : text.toLowerCase();

        if (t.contains("status") || t.contains("where is my refund") || t.contains("latest")) return AssistantIntent.REFUND_STATUS;
        if (t.contains("eta") || t.contains("when") || t.contains("how long") || t.contains("available")) return AssistantIntent.REFUND_ETA;
        if (t.contains("why") || t.contains("delayed") || t.contains("stuck") || t.contains("processing")) return AssistantIntent.WHY_DELAYED;
        if (t.contains("next step") || t.contains("what should i do") || t.contains("action")) return AssistantIntent.NEXT_STEPS;

        return AssistantIntent.UNKNOWN;
    }
}
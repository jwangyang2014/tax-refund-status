package com.intuit.taxrefund.assistant.core;

public record AssistantPlan(
    ConversationState nextState,
    boolean includeRefundStatus,
    boolean includeEta,
    boolean includePolicySnippets
) {}
package com.intuit.taxrefund.assistant.api.dto;

import java.util.List;

public record AssistantChatResponse(
    String answerMarkdown,
    List<Citation> citations,
    List<Action> actions,
    Confidence confidence
) {
    public enum Confidence { LOW, MEDIUM, HIGH }

    public record Citation(String docId, String quote) {}

    public record Action(ActionType type, String label) {}

    public enum ActionType { REFRESH, CONTACT_SUPPORT, SHOW_TRACKING }
}
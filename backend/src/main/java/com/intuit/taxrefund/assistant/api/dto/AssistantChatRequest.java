package com.intuit.taxrefund.assistant.api.dto;

import jakarta.validation.constraints.NotBlank;

public record AssistantChatRequest(@NotBlank String question) {}
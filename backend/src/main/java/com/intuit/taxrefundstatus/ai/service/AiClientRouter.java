package com.intuit.taxrefundstatus.ai.service;

import com.intuit.taxrefundstatus.ai.AiConfig;
import org.springframework.stereotype.Component;

@Component
public class AiClientRouter {
    private final AiConfig config;
    private final MockAiClient mockClient;
    private final OpenAiClient openAiClient;

    public AiClientRouter(AiConfig config, MockAiClient mockClient, OpenAiClient openAiClient) {
        this.config = config;
        this.mockClient = mockClient;
        this.openAiClient = openAiClient;
    }

    public AiClient getClient() {
        return switch (config.getProvider().toLowerCase()) {
            case "openai" -> openAiClient;
            default -> mockClient;
        };
    }
}

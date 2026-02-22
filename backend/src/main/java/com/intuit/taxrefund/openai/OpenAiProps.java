package com.intuit.taxrefund.openai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai")
public record OpenAiProps(
    String apiKey,
    String model
) {}
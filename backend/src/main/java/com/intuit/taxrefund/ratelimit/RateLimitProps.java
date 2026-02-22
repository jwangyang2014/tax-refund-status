package com.intuit.taxrefund.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ratelimit")
public record RateLimitProps(
    boolean enabled,
    Policy refundLatest,
    Policy assistantChat
) {
    public record Policy(int capacity, int refillPerMinute) {}
}
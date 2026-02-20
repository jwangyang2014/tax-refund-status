package com.intuit.taxrefund.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.cookies")
public record CookieProps(
   String refreshName,
   boolean secure,
   String sameSite
) {}

package com.ventixe.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ventixe.auth")
public record AuthProperties(
    String jwtSecret,
    String jwtIssuer,
    Duration jwtTtl,
    boolean secureCookie,
    List<String> allowedOrigins) {
}

package com.supporthub.triage.analysis.application;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "application.analysis")
public record TicketAnalysisProperties(
        boolean enabled,
        @Min(1) int batchSize,
        @Min(1) int maxAttempts,
        long pollDelayMs,
        Duration lockAtMostFor,
        Duration lockAtLeastFor,
        Duration stuckTimeout
) {
}

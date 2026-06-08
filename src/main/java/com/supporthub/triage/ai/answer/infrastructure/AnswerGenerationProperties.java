package com.supporthub.triage.ai.answer.infrastructure;

import com.supporthub.triage.ai.common.AiSafeLoggingProperties;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.ai.answer-generation")
public record AnswerGenerationProperties(
        String provider,
        String model,
        @Min(1) Integer maxOutputTokens,
        boolean safeLoggingEnabled
) implements AiSafeLoggingProperties {
}

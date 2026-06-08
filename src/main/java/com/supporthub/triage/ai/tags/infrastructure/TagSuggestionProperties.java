package com.supporthub.triage.ai.tags.infrastructure;

import com.supporthub.triage.ai.common.AiSafeLoggingProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.ai.tag-suggestion")
public record TagSuggestionProperties(
        String provider,
        String model,
        Integer maxOutputTokens,
        boolean safeLoggingEnabled
) implements AiSafeLoggingProperties {
}

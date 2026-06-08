package com.supporthub.triage.messaging.inbox.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "application.inbox")
public record InboxIgnoredEventProperties(
        Map<String, List<String>> ignoredEvents
) {

    public boolean isIgnored(String consumer, String eventType) {
        if (ignoredEvents == null || consumer == null || eventType == null) {
            return false;
        }

        return ignoredEvents.getOrDefault(consumer, List.of()).contains(eventType);
    }
}

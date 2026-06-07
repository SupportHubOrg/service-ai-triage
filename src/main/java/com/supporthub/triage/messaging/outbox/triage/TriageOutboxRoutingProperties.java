package com.supporthub.triage.messaging.outbox.triage;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "application.outbox.routing.triage")
public record TriageOutboxRoutingProperties(
        Map<String, String> eventTopics
) {

    public TriageOutboxRoutingProperties {
        eventTopics = eventTopics == null ? Map.of() : Map.copyOf(eventTopics);
    }
}

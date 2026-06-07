package com.supporthub.triage.messaging.event;

public record IntegrationEventContract(
        String key,
        String eventType,
        int version
) {
}

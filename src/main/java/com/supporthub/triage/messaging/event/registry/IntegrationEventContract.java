package com.supporthub.triage.messaging.event.registry;

public record IntegrationEventContract(
        String key,
        String eventType,
        int version
) {
}

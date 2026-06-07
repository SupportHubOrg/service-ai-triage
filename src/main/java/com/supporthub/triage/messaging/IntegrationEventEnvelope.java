package com.supporthub.triage.messaging;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Common wrapper around every integration event published by the service. Consumers can route and
 * deduplicate messages using metadata, while payload stays focused on business data.
 *
 * @param eventId      unique id of this event instance (useful for consumer-side dedup)
 * @param eventType    discriminator, e.g. {@code "AiAnalysisCompleted"}
 * @param eventVersion schema version of the payload
 * @param occurredAt   when the event was created
 * @param source       service name that produced the event
 * @param payload      business payload
 */
public record IntegrationEventEnvelope(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        String source,
        Object payload
) {

    public static IntegrationEventEnvelope of(String eventType,
                                              int version,
                                              String source,
                                              Object payload,
                                              Clock clock) {
        return new IntegrationEventEnvelope(UUID.randomUUID(), eventType, version, clock.instant(), source, payload);
    }
}

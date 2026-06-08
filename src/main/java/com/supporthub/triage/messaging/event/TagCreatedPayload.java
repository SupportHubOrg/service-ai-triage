package com.supporthub.triage.messaging.event;

import java.util.UUID;

@IntegrationEvent("tag-created")
public record TagCreatedPayload(
        UUID tagId,
        UUID organizationId,
        String tagName
) {
}

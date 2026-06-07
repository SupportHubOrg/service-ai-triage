package com.supporthub.triage.messaging.inbox.core.model;

import tools.jackson.databind.JsonNode;

public record InboxEventEnvelope(
        InboxEventMetadata metadata,
        JsonNode payload
) {
}

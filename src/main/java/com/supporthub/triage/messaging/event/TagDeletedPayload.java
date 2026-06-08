package com.supporthub.triage.messaging.event;

import java.util.UUID;

@IntegrationEvent("tag-deleted")
public record TagDeletedPayload(
        UUID tagId,
        UUID organizationId,
        UUID deletedById
) {
}

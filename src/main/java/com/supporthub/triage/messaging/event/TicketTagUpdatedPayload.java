package com.supporthub.triage.messaging.event;

import java.util.List;
import java.util.UUID;

@IntegrationEvent("ticket-tag-updated")
public record TicketTagUpdatedPayload(
        UUID ticketId,
        UUID organizationId,
        UUID updatedById,
        List<UUID> currentTags
) {
}

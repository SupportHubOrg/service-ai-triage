package com.supporthub.triage.messaging.event;

import com.supporthub.triage.messaging.event.annotation.IntegrationEvent;

import java.util.UUID;

@IntegrationEvent("ticket-created")
public record TicketCreatedPayload(
        UUID ticketId,
        UUID organizationId,
        UUID creatorId,
        String subject,
        String description
) {
}

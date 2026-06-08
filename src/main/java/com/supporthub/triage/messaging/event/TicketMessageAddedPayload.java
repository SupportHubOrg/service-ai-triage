package com.supporthub.triage.messaging.event;

import com.supporthub.triage.messaging.event.annotation.IntegrationEvent;

import java.util.UUID;

@IntegrationEvent("ticket-message-added")
public record TicketMessageAddedPayload(
        UUID ticketId,
        UUID organizationId,
        UUID messageId,
        UUID senderId,
        String senderType,
        String message
) {
}

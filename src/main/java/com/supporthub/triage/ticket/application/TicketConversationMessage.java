package com.supporthub.triage.ticket.application;

import com.supporthub.triage.shared.identity.MessageId;
import com.supporthub.triage.shared.identity.UserId;

import java.time.Instant;

public record TicketConversationMessage(
        MessageId messageId,
        UserId senderId,
        String senderType,
        String message,
        Instant createdAt
) {
}

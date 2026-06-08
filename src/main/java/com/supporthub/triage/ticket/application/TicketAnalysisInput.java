package com.supporthub.triage.ticket.application;

import com.supporthub.triage.shared.identity.OrganizationId;
import com.supporthub.triage.shared.identity.TagId;
import com.supporthub.triage.shared.identity.TicketId;
import com.supporthub.triage.shared.identity.UserId;

import java.util.List;

public record TicketAnalysisInput(
        TicketId ticketId,
        OrganizationId organizationId,
        UserId creatorId,
        String subject,
        String description,
        List<TicketConversationMessage> messages,
        List<TagId> tagIds
) {
}

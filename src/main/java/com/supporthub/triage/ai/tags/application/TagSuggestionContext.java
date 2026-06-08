package com.supporthub.triage.ai.tags.application;

import com.supporthub.triage.shared.identity.OrganizationId;
import com.supporthub.triage.shared.identity.TicketId;

import java.util.List;

/**
 * Everything the tag suggester needs to pick tags for a ticket: the conversation and the catalog of
 * tags the model may choose from.
 *
 * @param ticketId       source ticket (used for logging/correlation, not sent to the model)
 * @param organizationId owning organization (used for logging/correlation, not sent to the model)
 * @param conversation   the ticket conversation, ordered chronologically
 * @param candidateTags  the tags the model may choose from (the organization's AI-eligible tags)
 */
public record TagSuggestionContext(
        TicketId ticketId,
        OrganizationId organizationId,
        List<ConversationMessage> conversation,
        List<CandidateTag> candidateTags
) {
}

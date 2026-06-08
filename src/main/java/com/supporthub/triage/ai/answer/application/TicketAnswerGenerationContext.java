package com.supporthub.triage.ai.answer.application;

import com.supporthub.triage.ai.tags.application.SuggestedTags;
import com.supporthub.triage.shared.identity.TagId;
import com.supporthub.triage.ticket.application.TicketAnalysisInput;

import java.util.List;

public record TicketAnswerGenerationContext(
        TicketAnalysisInput ticket,
        SuggestedTags suggestedTags,
        List<TagId> retrievalTagIds
) {
    public TicketAnswerGenerationContext {
        retrievalTagIds = retrievalTagIds == null ? List.of() : List.copyOf(retrievalTagIds);
    }
}

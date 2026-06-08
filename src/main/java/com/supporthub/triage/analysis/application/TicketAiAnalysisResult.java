package com.supporthub.triage.analysis.application;

import com.supporthub.triage.shared.identity.AnalysisId;
import com.supporthub.triage.shared.identity.ArticleId;
import com.supporthub.triage.shared.identity.OrganizationId;
import com.supporthub.triage.shared.identity.TagId;
import com.supporthub.triage.shared.identity.TicketId;

import java.util.List;

/**
 * Outcome of a successful AI triage analysis of a ticket: the tags the model suggests, a draft
 * answer, the knowledge articles it grounded the answer on, and how confident it is. Transient
 * domain value object that the publisher maps onto the {@code AiAnalysisCompleted} integration event.
 */
public record TicketAiAnalysisResult(
        AnalysisId analysisId,
        TicketId ticketId,
        OrganizationId organizationId,
        List<TagId> tagSuggestions,
        String answerText,
        List<ArticleId> usedKnowledgeArticleIds,
        double confidenceScore
) {

    public TicketAiAnalysisResult {
        tagSuggestions = tagSuggestions == null ? List.of() : List.copyOf(tagSuggestions);
        usedKnowledgeArticleIds = usedKnowledgeArticleIds == null ? List.of() : List.copyOf(usedKnowledgeArticleIds);
    }
}

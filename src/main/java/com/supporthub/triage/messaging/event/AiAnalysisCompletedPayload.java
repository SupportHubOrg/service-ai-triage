package com.supporthub.triage.messaging.event;

import java.util.List;
import java.util.UUID;

@IntegrationEvent("ai-analysis-completed")
public record AiAnalysisCompletedPayload(
        UUID analysisId,
        UUID ticketId,
        UUID organizationId,
        List<UUID> tagSuggestions,
        String answerText,
        List<UUID> usedKnowledgeArticleIds,
        double confidenceScore
) {
}

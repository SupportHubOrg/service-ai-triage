package com.supporthub.triage.messaging.event;

import com.supporthub.triage.messaging.event.annotation.IntegrationEvent;
import com.supporthub.triage.messaging.event.annotation.MessageKey;

import java.util.List;
import java.util.UUID;

@IntegrationEvent("ai-analysis-completed")
public record AiAnalysisCompletedPayload(
        UUID analysisId,
        @MessageKey UUID ticketId,
        UUID organizationId,
        List<UUID> tagSuggestions,
        String answerText,
        List<UUID> usedKnowledgeArticleIds,
        double confidenceScore
) {
}

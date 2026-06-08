package com.supporthub.triage.messaging.outbox.triage;

import com.supporthub.triage.analysis.application.TicketAiAnalysisFailure;
import com.supporthub.triage.analysis.application.TicketAiAnalysisResult;
import com.supporthub.triage.messaging.event.AiAnalysisCompletedPayload;
import com.supporthub.triage.messaging.event.AiAnalysisFailedPayload;
import com.supporthub.triage.shared.identity.AnalysisIds;
import com.supporthub.triage.shared.identity.ArticleIds;
import com.supporthub.triage.shared.identity.OrganizationIdConverter;
import com.supporthub.triage.shared.identity.TagIds;
import com.supporthub.triage.shared.identity.TicketIds;
import org.springframework.stereotype.Component;

/**
 * Maps transient triage domain results onto the wire payloads. Identity value objects are unwrapped
 * to raw UUIDs through their sanctioned exit points, so the integration events stay decoupled from
 * the internal domain types.
 */
@Component
public class TriageOutboxPayloadFactory {

    public AiAnalysisCompletedPayload aiAnalysisCompleted(TicketAiAnalysisResult result) {
        return new AiAnalysisCompletedPayload(
                AnalysisIds.toRaw(result.analysisId()),
                TicketIds.toRaw(result.ticketId()),
                OrganizationIdConverter.toRaw(result.organizationId()),
                result.tagSuggestions().stream().map(TagIds::toRaw).toList(),
                result.answerText(),
                result.usedKnowledgeArticleIds().stream().map(ArticleIds::toRaw).toList(),
                result.confidenceScore()
        );
    }

    public AiAnalysisFailedPayload aiAnalysisFailed(TicketAiAnalysisFailure failure) {
        return new AiAnalysisFailedPayload(
                AnalysisIds.toRaw(failure.analysisId()),
                TicketIds.toRaw(failure.ticketId()),
                OrganizationIdConverter.toRaw(failure.organizationId()),
                failure.failureCode(),
                failure.failureMessage()
        );
    }
}

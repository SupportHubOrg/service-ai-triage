package com.supporthub.triage.messaging.event;

import java.util.UUID;

@IntegrationEvent("ai-analysis-failed")
public record AiAnalysisFailedPayload(
        UUID analysisId,
        UUID ticketId,
        UUID organizationId,
        AiAnalysisFailureCode failureCode,
        String failureMessage
) {
}

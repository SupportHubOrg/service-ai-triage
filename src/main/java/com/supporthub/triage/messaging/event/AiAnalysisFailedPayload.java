package com.supporthub.triage.messaging.event;

import com.supporthub.triage.messaging.event.annotation.IntegrationEvent;
import com.supporthub.triage.messaging.event.annotation.MessageKey;

import java.util.UUID;

@IntegrationEvent("ai-analysis-failed")
public record AiAnalysisFailedPayload(
        UUID analysisId,
        @MessageKey UUID ticketId,
        UUID organizationId,
        AiAnalysisFailureCode failureCode,
        String failureMessage
) {
}

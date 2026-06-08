package com.supporthub.triage.analysis.application;

import com.supporthub.triage.messaging.event.AiAnalysisFailureCode;
import com.supporthub.triage.shared.identity.AnalysisId;
import com.supporthub.triage.shared.identity.OrganizationId;
import com.supporthub.triage.shared.identity.TicketId;

/**
 * Outcome of an AI triage analysis that could not complete: a coded reason and a human-readable
 * message. Transient domain value object that the publisher maps onto the {@code AiAnalysisFailed}
 * integration event (a compensating signal that hands the ticket back for human review).
 */
public record TicketAiAnalysisFailure(
        AnalysisId analysisId,
        TicketId ticketId,
        OrganizationId organizationId,
        AiAnalysisFailureCode failureCode,
        String failureMessage
) {
}

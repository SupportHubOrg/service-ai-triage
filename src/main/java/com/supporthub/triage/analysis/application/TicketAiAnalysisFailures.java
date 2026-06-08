package com.supporthub.triage.analysis.application;

import com.supporthub.triage.analysis.persistence.TicketAnalysisEntity;
import com.supporthub.triage.messaging.event.AiAnalysisFailureCode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class TicketAiAnalysisFailures {

    static TicketAiAnalysisFailure invalidTicketData(TicketAnalysisEntity analysis, String message) {
        return new TicketAiAnalysisFailure(
                analysis.getAnalysisId(),
                analysis.getTicketId(),
                analysis.getOrganizationId(),
                AiAnalysisFailureCode.INVALID_TICKET_DATA,
                message
        );
    }

    static TicketAiAnalysisFailure invalidAiResponse(TicketAnalysisEntity analysis, String message) {
        return new TicketAiAnalysisFailure(
                analysis.getAnalysisId(),
                analysis.getTicketId(),
                analysis.getOrganizationId(),
                AiAnalysisFailureCode.AI_RESPONSE_INVALID,
                message
        );
    }

    static TicketAiAnalysisFailure technical(TicketAnalysisEntity analysis, RuntimeException exception) {
        return new TicketAiAnalysisFailure(
                analysis.getAnalysisId(),
                analysis.getTicketId(),
                analysis.getOrganizationId(),
                AiAnalysisFailureCode.TRIAGE_TECHNICAL_ERROR,
                failureMessage(exception)
        );
    }

    private static String failureMessage(RuntimeException exception) {
        var message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message;
    }
}

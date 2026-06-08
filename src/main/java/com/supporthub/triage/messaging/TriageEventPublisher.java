package com.supporthub.triage.messaging;

import com.supporthub.triage.analysis.application.TicketAiAnalysisFailure;
import com.supporthub.triage.analysis.application.TicketAiAnalysisResult;

/**
 * Outbound integration events emitted by this service. These form the public contract the
 * ticket-management service subscribes to. The implementation stores events in the transactional
 * outbox; Kafka delivery is handled asynchronously by the outbox publisher.
 */
public interface TriageEventPublisher {

    /** Emitted when AI triage finished a ticket: suggested tags, a draft answer and a confidence score. */
    void publishAiAnalysisCompleted(TicketAiAnalysisResult result);

    /** Emitted when AI triage could not analyze a ticket (compensating signal for human review). */
    void publishAiAnalysisFailed(TicketAiAnalysisFailure failure);
}

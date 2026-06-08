package com.supporthub.triage.messaging.outbox.triage;

import com.supporthub.triage.analysis.application.TicketAiAnalysisFailure;
import com.supporthub.triage.analysis.application.TicketAiAnalysisResult;
import com.supporthub.triage.messaging.TriageEventPublisher;
import com.supporthub.triage.messaging.outbox.core.persistence.OutboxEventWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Domain-facing implementation of {@link TriageEventPublisher}. It records events into the outbox;
 * delivery to Kafka is handled later by the outbox publisher. The Kafka message key is derived from
 * each payload's {@code @MessageKey} component (the source ticket) by the outbox writer.
 */
@Component
@RequiredArgsConstructor
public class TriageOutboxEventRecorder implements TriageEventPublisher {

    private final OutboxEventWriter eventWriter;
    private final TriageOutboxPayloadFactory payloadFactory;

    @Override
    public void publishAiAnalysisCompleted(TicketAiAnalysisResult result) {
        eventWriter.append(payloadFactory.aiAnalysisCompleted(result));
    }

    @Override
    public void publishAiAnalysisFailed(TicketAiAnalysisFailure failure) {
        eventWriter.append(payloadFactory.aiAnalysisFailed(failure));
    }
}

package com.supporthub.triage.messaging.outbox.triage;

import com.supporthub.triage.messaging.IntegrationEventEnvelope;
import com.supporthub.triage.messaging.outbox.core.model.OutboxEventRoute;
import com.supporthub.triage.messaging.outbox.core.spi.OutboxRouteResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TriageOutboxRouteResolver implements OutboxRouteResolver {

    private final TriageOutboxRoutingProperties properties;

    @Override
    public OutboxEventRoute resolve(String messageKey, String contractKey, IntegrationEventEnvelope event) {
        if (messageKey == null || messageKey.isBlank()) {
            throw new TriageOutboxRoutingException("Message key is required for " + event.eventType());
        }

        var topic = properties.eventTopics().get(contractKey);
        if (topic == null || topic.isBlank()) {
            throw new TriageOutboxRoutingException("No outbox topic configured for " + contractKey);
        }

        return new OutboxEventRoute(topic, messageKey);
    }
}

package com.supporthub.triage.messaging.outbox.core.spi;

import com.supporthub.triage.messaging.IntegrationEventEnvelope;
import com.supporthub.triage.messaging.outbox.core.model.OutboxEventRoute;

/** SPI a service implements to choose the destination topic and message key for an event. */
public interface OutboxRouteResolver {

    OutboxEventRoute resolve(String messageKey, IntegrationEventEnvelope event);
}

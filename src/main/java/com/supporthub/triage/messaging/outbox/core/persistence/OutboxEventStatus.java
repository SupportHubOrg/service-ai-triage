package com.supporthub.triage.messaging.outbox.core.persistence;

public enum OutboxEventStatus {
    PENDING,
    IN_PROGRESS,
    SENT
}

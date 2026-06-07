package com.supporthub.triage.messaging.inbox.core.handler;

public record InboxHandlerKey(
        String consumer,
        String eventType,
        int version
) {
}

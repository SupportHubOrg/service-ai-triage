package com.supporthub.triage.messaging.outbox.triage;

import com.supporthub.triage.messaging.outbox.core.OutboxException;

public class TriageOutboxRoutingException extends OutboxException {

    public TriageOutboxRoutingException(String message) {
        super(message);
    }
}

package com.supporthub.triage.messaging.inbox.core.error;

public class InboxException extends RuntimeException {

    public InboxException(String message) {
        super(message);
    }

    public InboxException(String message, Throwable cause) {
        super(message, cause);
    }
}

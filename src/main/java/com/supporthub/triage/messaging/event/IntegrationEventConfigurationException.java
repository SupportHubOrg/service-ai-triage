package com.supporthub.triage.messaging.event;

public class IntegrationEventConfigurationException extends RuntimeException {

    public IntegrationEventConfigurationException(String message) {
        super(message);
    }

    public IntegrationEventConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}

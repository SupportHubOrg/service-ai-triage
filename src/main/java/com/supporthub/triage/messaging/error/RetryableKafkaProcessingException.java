package com.supporthub.triage.messaging.error;

public class RetryableKafkaProcessingException extends RuntimeException {

    public RetryableKafkaProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

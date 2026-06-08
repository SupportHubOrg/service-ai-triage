package com.supporthub.triage.ai.answer.application;

public class AnswerGenerationException extends RuntimeException {

    public AnswerGenerationException(String message) {
        super(message);
    }

    public AnswerGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.supporthub.triage.ai.tags.application;

/**
 * Thrown by a {@link TicketTagSuggester} when it cannot produce a usable suggestion (e.g. the AI
 * provider is unavailable or returns an unusable result). Treated by the analysis flow as a
 * recoverable failure.
 */
public class TagSuggestionException extends RuntimeException {

    public TagSuggestionException(String message) {
        super(message);
    }

    public TagSuggestionException(String message, Throwable cause) {
        super(message, cause);
    }
}

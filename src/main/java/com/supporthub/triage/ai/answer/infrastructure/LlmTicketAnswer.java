package com.supporthub.triage.ai.answer.infrastructure;

public record LlmTicketAnswer(
        String answerText,
        Double confidenceScore
) {
}

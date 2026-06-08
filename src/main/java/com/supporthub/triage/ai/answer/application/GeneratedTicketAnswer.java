package com.supporthub.triage.ai.answer.application;

import com.supporthub.triage.shared.identity.ArticleId;

import java.util.List;

public record GeneratedTicketAnswer(
        String answerText,
        List<ArticleId> usedKnowledgeArticleIds,
        double confidenceScore
) {
    public GeneratedTicketAnswer {
        if (answerText == null || answerText.isBlank()) {
            throw new AnswerGenerationException("Generated answer text is empty");
        }
        if (Double.isNaN(confidenceScore) || confidenceScore < 0 || confidenceScore > 1) {
            throw new AnswerGenerationException("Generated confidence score must be between 0 and 1");
        }
        answerText = answerText.trim();
        usedKnowledgeArticleIds = usedKnowledgeArticleIds == null ? List.of() : List.copyOf(usedKnowledgeArticleIds);
    }
}

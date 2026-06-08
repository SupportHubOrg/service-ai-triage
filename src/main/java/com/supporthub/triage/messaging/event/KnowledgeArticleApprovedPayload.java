package com.supporthub.triage.messaging.event;

import com.supporthub.triage.messaging.event.annotation.IntegrationEvent;

import java.util.List;
import java.util.UUID;

@IntegrationEvent("knowledge-article-approved")
public record KnowledgeArticleApprovedPayload(
        UUID articleId,
        UUID draftId,
        UUID sourceTicketId,
        UUID organizationId,
        UUID publishedById,
        String title,
        String articleContent,
        List<UUID> tags
) {
}

package com.supporthub.triage.messaging.consumer;

import com.supporthub.triage.knowledge.application.KnowledgeArticleProjection;
import com.supporthub.triage.messaging.event.KnowledgeArticleApprovedPayload;
import com.supporthub.triage.messaging.inbox.core.annotation.InboxConsumer;
import com.supporthub.triage.messaging.inbox.core.annotation.InboxHandler;
import com.supporthub.triage.messaging.inbox.core.context.InboxConsumers;
import com.supporthub.triage.messaging.inbox.core.context.InboxEventContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@InboxConsumer(InboxConsumers.KNOWLEDGE_MANAGEMENT)
@RequiredArgsConstructor
public class KnowledgeManagementInboxHandlers {

    private final KnowledgeArticleProjection articleProjection;

    @InboxHandler
    void onKnowledgeArticleApproved(KnowledgeArticleApprovedPayload payload, InboxEventContext context) {
        articleProjection.project(payload, context);
    }
}

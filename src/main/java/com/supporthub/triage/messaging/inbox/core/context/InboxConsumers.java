package com.supporthub.triage.messaging.inbox.core.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InboxConsumers {

    public static final String TICKET_MANAGEMENT = "ai-triage.ticket-management";
    public static final String ORGANIZATION_ACCESS = "ai-triage.organization-access";
    public static final String KNOWLEDGE_MANAGEMENT = "ai-triage.knowledge-management";

}

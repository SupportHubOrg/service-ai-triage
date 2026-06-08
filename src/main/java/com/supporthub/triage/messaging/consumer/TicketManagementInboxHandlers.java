package com.supporthub.triage.messaging.consumer;

import com.supporthub.triage.messaging.event.TagAiSettingUpdatedPayload;
import com.supporthub.triage.messaging.event.TagCreatedPayload;
import com.supporthub.triage.messaging.event.TagDeletedPayload;
import com.supporthub.triage.messaging.inbox.core.annotation.InboxConsumer;
import com.supporthub.triage.messaging.inbox.core.annotation.InboxHandler;
import com.supporthub.triage.messaging.inbox.core.context.InboxConsumers;
import com.supporthub.triage.tag.application.OrganizationTagProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@InboxConsumer(InboxConsumers.TICKET_MANAGEMENT)
@RequiredArgsConstructor
public class TicketManagementInboxHandlers {

    private final OrganizationTagProjection tagProjection;

    @InboxHandler
    void onTagCreated(TagCreatedPayload payload) {
        tagProjection.project(payload);
    }

    @InboxHandler
    void onTagDeleted(TagDeletedPayload payload) {
        tagProjection.project(payload);
    }

    @InboxHandler
    void onTagAiSettingUpdated(TagAiSettingUpdatedPayload payload) {
        tagProjection.project(payload);
    }
}

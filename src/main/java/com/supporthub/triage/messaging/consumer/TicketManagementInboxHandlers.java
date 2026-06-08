package com.supporthub.triage.messaging.consumer;

import com.supporthub.triage.messaging.event.TagAiSettingUpdatedPayload;
import com.supporthub.triage.messaging.event.TagCreatedPayload;
import com.supporthub.triage.messaging.event.TagDeletedPayload;
import com.supporthub.triage.messaging.event.TicketCreatedPayload;
import com.supporthub.triage.messaging.event.TicketMessageAddedPayload;
import com.supporthub.triage.messaging.event.TicketTagUpdatedPayload;
import com.supporthub.triage.messaging.inbox.core.annotation.InboxConsumer;
import com.supporthub.triage.messaging.inbox.core.annotation.InboxHandler;
import com.supporthub.triage.messaging.inbox.core.context.InboxConsumers;
import com.supporthub.triage.messaging.inbox.core.context.InboxEventContext;
import com.supporthub.triage.analysis.application.TicketAnalysisJobStore;
import com.supporthub.triage.shared.identity.OrganizationId;
import com.supporthub.triage.shared.identity.TicketId;
import com.supporthub.triage.tag.application.OrganizationTagProjection;
import com.supporthub.triage.ticket.application.TicketProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@InboxConsumer(InboxConsumers.TICKET_MANAGEMENT)
@RequiredArgsConstructor
public class TicketManagementInboxHandlers {

    private final OrganizationTagProjection tagProjection;
    private final TicketProjection ticketProjection;
    private final TicketAnalysisJobStore analysisJobStore;

    @InboxHandler
    void onTicketCreated(TicketCreatedPayload payload) {
        ticketProjection.project(payload);
        analysisJobStore.requestAnalysis(
                TicketId.of(payload.ticketId()),
                OrganizationId.of(payload.organizationId())
        );
    }

    @InboxHandler
    void onTicketMessageAdded(TicketMessageAddedPayload payload, InboxEventContext context) {
        ticketProjection.project(payload, context);
    }

    @InboxHandler
    void onTicketTagUpdated(TicketTagUpdatedPayload payload) {
        ticketProjection.project(payload);
    }

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

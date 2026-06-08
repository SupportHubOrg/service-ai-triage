package com.supporthub.triage.ticket.application;

import com.supporthub.triage.messaging.event.TicketCreatedPayload;
import com.supporthub.triage.messaging.event.TicketMessageAddedPayload;
import com.supporthub.triage.messaging.event.TicketTagUpdatedPayload;
import com.supporthub.triage.messaging.inbox.core.context.InboxEventContext;
import com.supporthub.triage.shared.identity.MessageId;
import com.supporthub.triage.shared.identity.OrganizationId;
import com.supporthub.triage.shared.identity.TagId;
import com.supporthub.triage.shared.identity.TicketId;
import com.supporthub.triage.shared.identity.UserId;
import com.supporthub.triage.ticket.persistence.TicketMessageProjectionEntity;
import com.supporthub.triage.ticket.persistence.TicketMessageProjectionRepository;
import com.supporthub.triage.ticket.persistence.TicketProjectionEntity;
import com.supporthub.triage.ticket.persistence.TicketProjectionRepository;
import com.supporthub.triage.ticket.persistence.TicketTagProjectionEntity;
import com.supporthub.triage.ticket.persistence.TicketTagProjectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketProjection {

    private final TicketProjectionRepository ticketRepository;
    private final TicketMessageProjectionRepository messageRepository;
    private final TicketTagProjectionRepository tagRepository;
    private final Clock clock;

    @Transactional
    public void project(TicketCreatedPayload payload) {
        validateTicket(payload.ticketId(), payload.organizationId());

        var now = Instant.now(clock);
        var organizationId = OrganizationId.of(payload.organizationId());
        var ticket = findOrCreateTicket(TicketId.of(payload.ticketId()), organizationId, now);
        ticket.applyCreated(
                organizationId,
                creatorId(payload),
                payload.subject(),
                payload.description(),
                now
        );
        ticketRepository.save(ticket);
    }

    @Transactional
    public void project(TicketMessageAddedPayload payload, InboxEventContext context) {
        validateMessage(payload);

        var now = Instant.now(clock);
        var organizationId = OrganizationId.of(payload.organizationId());
        var ticket = findOrCreateTicket(TicketId.of(payload.ticketId()), organizationId, now);
        ticket.touchOrganization(organizationId, now);
        ticketRepository.save(ticket);

        var messageId = MessageId.of(payload.messageId());
        if (messageRepository.existsById(messageId)) {
            return;
        }

        messageRepository.save(TicketMessageProjectionEntity.added(
                messageId,
                TicketId.of(payload.ticketId()),
                organizationId,
                UserId.of(payload.senderId()),
                payload.senderType(),
                payload.message(),
                context.timestamp()
        ));
    }

    @Transactional
    public void project(TicketTagUpdatedPayload payload) {
        validateTicket(payload.ticketId(), payload.organizationId());

        var now = Instant.now(clock);
        var ticketId = TicketId.of(payload.ticketId());
        var organizationId = OrganizationId.of(payload.organizationId());
        var ticket = findOrCreateTicket(ticketId, organizationId, now);
        ticket.touchOrganization(organizationId, now);
        ticketRepository.save(ticket);

        tagRepository.deleteByTicketId(ticketId);
        tagRepository.saveAll(tagIds(payload)
                .stream()
                .map(tagId -> TicketTagProjectionEntity.assigned(
                        ticketId,
                        tagId,
                        organizationId,
                        now
                ))
                .toList());
    }

    @Transactional(readOnly = true)
    public Optional<TicketAnalysisInput> findAnalysisInput(TicketId ticketId) {
        return ticketRepository.findById(ticketId)
                .map(ticket -> new TicketAnalysisInput(
                        ticket.getTicketId(),
                        ticket.getOrganizationId(),
                        ticket.getCreatorId(),
                        ticket.getSubject(),
                        ticket.getDescription(),
                        messages(ticketId),
                        tagIds(ticketId)
                ));
    }

    private TicketProjectionEntity findOrCreateTicket(TicketId ticketId, OrganizationId organizationId, Instant now) {
        return ticketRepository.findById(ticketId)
                .orElseGet(() -> TicketProjectionEntity.placeholder(ticketId, organizationId, now));
    }

    private List<TicketConversationMessage> messages(TicketId ticketId) {
        return messageRepository.findByTicketIdOrderByCreatedAtAscMessageIdAsc(ticketId)
                .stream()
                .map(message -> new TicketConversationMessage(
                        message.getMessageId(),
                        message.getSenderId(),
                        message.getSenderType(),
                        message.getMessage(),
                        message.getCreatedAt()
                ))
                .toList();
    }

    private List<TagId> tagIds(TicketId ticketId) {
        return tagRepository.findByIdTicketId(ticketId)
                .stream()
                .map(tag -> tag.getId().getTagId())
                .toList();
    }

    private List<TagId> tagIds(TicketTagUpdatedPayload payload) {
        if (payload.currentTags() == null) {
            return List.of();
        }
        return payload.currentTags()
                .stream()
                .filter(tagId -> tagId != null)
                .distinct()
                .map(TagId::of)
                .toList();
    }

    private UserId creatorId(TicketCreatedPayload payload) {
        return payload.creatorId() == null ? null : UserId.of(payload.creatorId());
    }

    private void validateMessage(TicketMessageAddedPayload payload) {
        validateTicket(payload.ticketId(), payload.organizationId());
        require(payload.messageId(), "Message id is required");
        require(payload.senderId(), "Message sender id is required");
        require(payload.senderType(), "Message sender type is required");
        require(payload.message(), "Message text is required");
    }

    private void validateTicket(UUID ticketId, UUID organizationId) {
        require(ticketId, "Ticket id is required");
        require(organizationId, "Organization id is required");
    }

    private void require(UUID value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private void require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}

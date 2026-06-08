package com.supporthub.triage.ticket.persistence;

import com.supporthub.triage.shared.identity.MessageId;
import com.supporthub.triage.shared.identity.OrganizationId;
import com.supporthub.triage.shared.identity.TicketId;
import com.supporthub.triage.shared.identity.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "ticket_message_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketMessageProjectionEntity {

    @EmbeddedId
    private MessageId messageId;

    @Embedded
    private TicketId ticketId;

    @Column(name = "organization_id", nullable = false)
    private OrganizationId organizationId;

    @Column(name = "sender_id", nullable = false)
    private UserId senderId;

    @Column(name = "sender_type", nullable = false)
    private String senderType;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static TicketMessageProjectionEntity added(MessageId messageId,
                                                      TicketId ticketId,
                                                      OrganizationId organizationId,
                                                      UserId senderId,
                                                      String senderType,
                                                      String message,
                                                      Instant createdAt) {
        var entity = new TicketMessageProjectionEntity();
        entity.messageId = messageId;
        entity.ticketId = ticketId;
        entity.organizationId = organizationId;
        entity.senderId = senderId;
        entity.senderType = senderType.trim();
        entity.message = message.trim();
        entity.createdAt = createdAt;
        return entity;
    }
}

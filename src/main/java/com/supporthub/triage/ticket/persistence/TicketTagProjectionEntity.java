package com.supporthub.triage.ticket.persistence;

import com.supporthub.triage.shared.identity.OrganizationId;
import com.supporthub.triage.shared.identity.TagId;
import com.supporthub.triage.shared.identity.TicketId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "ticket_tag_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketTagProjectionEntity {

    @EmbeddedId
    private TicketTagProjectionId id;

    @Column(name = "organization_id", nullable = false)
    private OrganizationId organizationId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static TicketTagProjectionEntity assigned(TicketId ticketId,
                                                     TagId tagId,
                                                     OrganizationId organizationId,
                                                     Instant now) {
        var entity = new TicketTagProjectionEntity();
        entity.id = TicketTagProjectionId.of(ticketId, tagId);
        entity.organizationId = organizationId;
        entity.createdAt = now;
        return entity;
    }
}

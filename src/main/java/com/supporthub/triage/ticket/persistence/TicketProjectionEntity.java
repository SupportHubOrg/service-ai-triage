package com.supporthub.triage.ticket.persistence;

import com.supporthub.triage.shared.identity.OrganizationId;
import com.supporthub.triage.shared.identity.TicketId;
import com.supporthub.triage.shared.identity.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "ticket_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketProjectionEntity {

    @EmbeddedId
    private TicketId ticketId;

    @Column(name = "organization_id", nullable = false)
    private OrganizationId organizationId;

    @Column(name = "creator_id")
    private UserId creatorId;

    @Column(name = "subject")
    private String subject;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static TicketProjectionEntity placeholder(TicketId ticketId, OrganizationId organizationId, Instant now) {
        var ticket = new TicketProjectionEntity();
        ticket.ticketId = ticketId;
        ticket.organizationId = organizationId;
        ticket.createdAt = now;
        ticket.updatedAt = now;
        return ticket;
    }

    public void applyCreated(OrganizationId organizationId,
                             UserId creatorId,
                             String subject,
                             String description,
                             Instant now) {
        this.organizationId = organizationId;
        this.creatorId = creatorId;
        this.subject = trim(subject);
        this.description = trim(description);
        this.updatedAt = now;
    }

    public void touchOrganization(OrganizationId organizationId, Instant now) {
        this.organizationId = organizationId;
        this.updatedAt = now;
    }

    private String trim(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}

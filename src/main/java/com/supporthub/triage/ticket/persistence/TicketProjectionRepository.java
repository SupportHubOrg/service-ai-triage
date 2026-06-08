package com.supporthub.triage.ticket.persistence;

import com.supporthub.triage.shared.identity.TicketId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketProjectionRepository extends JpaRepository<TicketProjectionEntity, TicketId> {
}

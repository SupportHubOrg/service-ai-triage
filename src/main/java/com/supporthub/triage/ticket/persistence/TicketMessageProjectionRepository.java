package com.supporthub.triage.ticket.persistence;

import com.supporthub.triage.shared.identity.MessageId;
import com.supporthub.triage.shared.identity.TicketId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketMessageProjectionRepository extends JpaRepository<TicketMessageProjectionEntity, MessageId> {

    List<TicketMessageProjectionEntity> findByTicketIdOrderByCreatedAtAscMessageIdAsc(TicketId ticketId);
}

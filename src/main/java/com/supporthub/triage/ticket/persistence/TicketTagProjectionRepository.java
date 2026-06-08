package com.supporthub.triage.ticket.persistence;

import com.supporthub.triage.shared.identity.TicketId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TicketTagProjectionRepository extends JpaRepository<TicketTagProjectionEntity, TicketTagProjectionId> {

    List<TicketTagProjectionEntity> findByIdTicketId(TicketId ticketId);

    @Modifying
    @Query("delete from TicketTagProjectionEntity tag where tag.id.ticketId = :ticketId")
    void deleteByTicketId(TicketId ticketId);
}

package com.supporthub.triage.analysis.persistence;

import com.supporthub.triage.shared.identity.AnalysisId;
import com.supporthub.triage.shared.identity.TicketId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface TicketAnalysisRepository extends JpaRepository<TicketAnalysisEntity, AnalysisId> {

    boolean existsByTicketId(TicketId ticketId);

    @Query(
            value = """
                    select *
                    from ticket_analysis
                    where status = :status
                      and attempts < :maxAttempts
                    order by updated_at, analysis_id
                    limit :batchSize
                    for update skip locked
                    """,
            nativeQuery = true
    )
    List<TicketAnalysisEntity> findPendingForUpdate(@Param("status") String status,
                                                    @Param("batchSize") int batchSize,
                                                    @Param("maxAttempts") int maxAttempts);

    @Modifying
    @Query(
            value = """
                    update ticket_analysis
                    set status = :pendingStatus,
                        last_error = 'Analysis stage was interrupted and marked pending for retry',
                        locked_at = null,
                        updated_at = :now
                    where status = :inProgressStatus
                      and updated_at < :cutoff
                    """,
            nativeQuery = true
    )
    int markStuckStageAsPending(@Param("inProgressStatus") String inProgressStatus,
                                @Param("pendingStatus") String pendingStatus,
                                @Param("cutoff") Instant cutoff,
                                @Param("now") Instant now);
}

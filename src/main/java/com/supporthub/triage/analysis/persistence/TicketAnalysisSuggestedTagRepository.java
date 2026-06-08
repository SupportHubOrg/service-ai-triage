package com.supporthub.triage.analysis.persistence;

import com.supporthub.triage.shared.identity.AnalysisId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketAnalysisSuggestedTagRepository
        extends JpaRepository<TicketAnalysisSuggestedTagEntity, TicketAnalysisSuggestedTagId> {

    List<TicketAnalysisSuggestedTagEntity> findByIdAnalysisIdOrderByTagName(AnalysisId analysisId);

    @Modifying
    @Query("""
            delete from TicketAnalysisSuggestedTagEntity tag
            where tag.id.analysisId = :analysisId
            """)
    void deleteByAnalysisId(@Param("analysisId") AnalysisId analysisId);
}

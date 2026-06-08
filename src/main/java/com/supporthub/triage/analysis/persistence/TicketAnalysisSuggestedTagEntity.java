package com.supporthub.triage.analysis.persistence;

import com.supporthub.triage.ai.tags.application.SuggestedTag;
import com.supporthub.triage.shared.identity.AnalysisId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ticket_analysis_suggested_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketAnalysisSuggestedTagEntity {

    @EmbeddedId
    private TicketAnalysisSuggestedTagId id;

    @Column(name = "tag_name", nullable = false)
    private String tagName;

    public static TicketAnalysisSuggestedTagEntity selected(AnalysisId analysisId, SuggestedTag tag) {
        var entity = new TicketAnalysisSuggestedTagEntity();
        entity.id = TicketAnalysisSuggestedTagId.of(analysisId, tag.id());
        entity.tagName = tag.name();
        return entity;
    }

    public SuggestedTag toSuggestedTag() {
        return new SuggestedTag(id.getTagId(), tagName);
    }
}

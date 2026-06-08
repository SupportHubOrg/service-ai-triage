package com.supporthub.triage.analysis.persistence;

import com.supporthub.triage.shared.identity.AnalysisId;
import com.supporthub.triage.shared.identity.TagId;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "of")
public class TicketAnalysisSuggestedTagId implements Serializable {

    @Embedded
    private AnalysisId analysisId;

    @Embedded
    private TagId tagId;
}

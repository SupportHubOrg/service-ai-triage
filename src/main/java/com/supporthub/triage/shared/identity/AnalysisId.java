package com.supporthub.triage.shared.identity;

import com.supporthub.triage.shared.error.BusinessValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Identity of a single AI triage analysis run. It is persisted as the analysis job identifier and
 * also carried on outbound {@code AiAnalysisCompleted}/{@code AiAnalysisFailed} events.
 */
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED) // for JPA
public final class AnalysisId {

    @Column(name = "analysis_id")
    private UUID value;

    private AnalysisId(UUID value) {
        if (value == null) {
            throw new BusinessValidationException("Invalid analysis id");
        }
        this.value = value;
    }

    public static AnalysisId of(UUID value) {
        return new AnalysisId(value);
    }

    public static AnalysisId generate() {
        return new AnalysisId(UUID.randomUUID());
    }

    UUID unwrap() {
        return value;
    }

    public boolean sameAs(AnalysisId other) {
        return other != null && this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "AnalysisId[" + value + "]";
    }
}

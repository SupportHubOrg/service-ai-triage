package com.supporthub.triage.shared.identity;

import com.supporthub.triage.shared.error.BusinessValidationException;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * Identity of a single AI triage analysis run. Unlike the other identities this one is never
 * persisted: it is a transient correlation id minted per analysis and carried on the outbound
 * {@code AiAnalysisCompleted}/{@code AiAnalysisFailed} events, so it needs no JPA mapping.
 */
@EqualsAndHashCode
public final class AnalysisId {

    private final UUID value;

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

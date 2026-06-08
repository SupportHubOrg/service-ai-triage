package com.supporthub.triage.shared.identity;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Sanctioned conversion of {@link AnalysisId} to its raw wire/JSON form. {@code AnalysisId} itself
 * is an {@code @EmbeddedId}, so no JPA {@code AttributeConverter} is needed for persistence.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AnalysisIds {

    public static UUID toRaw(AnalysisId id) {
        return id == null ? null : id.unwrap();
    }
}

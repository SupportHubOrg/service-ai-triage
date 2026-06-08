package com.supporthub.triage.shared.identity;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Sanctioned conversion of {@link TagId} to its raw wire/JSON form. {@code TagId} is used as a
 * primary/composite key ({@code @EmbeddedId}), so no JPA {@code AttributeConverter} is needed.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TagIds {

    public static UUID toRaw(TagId id) {
        return id == null ? null : id.unwrap();
    }
}

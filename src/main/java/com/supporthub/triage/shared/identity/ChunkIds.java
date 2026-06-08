package com.supporthub.triage.shared.identity;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Sanctioned conversion of {@link ChunkId} to its raw wire/JSON form. {@code ChunkId} itself is an
 * {@code @EmbeddedId}, so no JPA {@code AttributeConverter} is needed for persistence.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChunkIds {

    public static UUID toRaw(ChunkId id) {
        return id == null ? null : id.unwrap();
    }
}

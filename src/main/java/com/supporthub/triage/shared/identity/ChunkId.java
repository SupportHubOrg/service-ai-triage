package com.supporthub.triage.shared.identity;

import com.supporthub.triage.shared.error.BusinessValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Identity of a projected knowledge article chunk. Mapped as an {@code @Embeddable} so the domain
 * works with the value object while the database stores the raw UUID column (Hibernate forbids
 * attribute converters on identifier attributes).
 */
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED) // for JPA
public final class ChunkId {

    @Column(name = "chunk_id")
    private UUID value;

    private ChunkId(UUID value) {
        if (value == null) {
            throw new BusinessValidationException("Invalid chunk id");
        }
        this.value = value;
    }

    public static ChunkId of(UUID value) {
        return new ChunkId(value);
    }

    UUID unwrap() {
        return value;
    }

    public boolean sameAs(ChunkId other) {
        return other != null && this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "ChunkId[" + value + "]";
    }
}

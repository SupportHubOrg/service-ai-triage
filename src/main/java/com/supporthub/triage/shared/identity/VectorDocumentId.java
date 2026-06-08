package com.supporthub.triage.shared.identity;

import com.supporthub.triage.shared.error.BusinessValidationException;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode
public final class VectorDocumentId {

    private final UUID value;

    private VectorDocumentId(UUID value) {
        if (value == null) {
            throw new BusinessValidationException("Invalid vector document id");
        }
        this.value = value;
    }

    public static VectorDocumentId of(UUID value) {
        return new VectorDocumentId(value);
    }

    UUID unwrap() {
        return value;
    }

    public boolean sameAs(VectorDocumentId other) {
        return other != null && this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "VectorDocumentId[" + value + "]";
    }
}

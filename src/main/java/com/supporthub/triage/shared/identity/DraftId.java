package com.supporthub.triage.shared.identity;

import com.supporthub.triage.shared.error.BusinessValidationException;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode
public final class DraftId {

    private final UUID value;

    private DraftId(UUID value) {
        if (value == null) {
            throw new BusinessValidationException("Invalid draft id");
        }
        this.value = value;
    }

    public static DraftId of(UUID value) {
        return new DraftId(value);
    }

    UUID unwrap() {
        return value;
    }

    public boolean sameAs(DraftId other) {
        return other != null && this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "DraftId[" + value + "]";
    }
}

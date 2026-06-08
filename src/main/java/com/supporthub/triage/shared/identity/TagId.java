package com.supporthub.triage.shared.identity;

import com.supporthub.triage.shared.error.BusinessValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Identity of a projected organization tag. Mapped as an {@code @Embeddable} so the domain works with
 * the value object while the database stores the raw UUID column (Hibernate forbids attribute
 * converters on identifier attributes; tag ids are used as primary and composite keys).
 */
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED) // for JPA
public final class TagId {

    @Column(name = "tag_id")
    private UUID value;

    private TagId(UUID value) {
        if (value == null) {
            throw new BusinessValidationException("Invalid tag id");
        }
        this.value = value;
    }

    public static TagId of(UUID value) {
        return new TagId(value);
    }

    UUID unwrap() {
        return value;
    }

    public boolean sameAs(TagId other) {
        return other != null && this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "TagId[" + value + "]";
    }
}

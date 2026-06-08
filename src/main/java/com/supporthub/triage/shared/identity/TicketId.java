package com.supporthub.triage.shared.identity;

import com.supporthub.triage.shared.error.BusinessValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Identity of a projected ticket. Mapped as an {@code @Embeddable} so the domain works with the value
 * object while the database stores the raw UUID column (Hibernate forbids attribute converters on
 * identifier attributes). Reused as a foreign-key column via {@code @AttributeOverride} where the
 * column name differs from {@code ticket_id}.
 */
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED) // for JPA
public final class TicketId {

    @Column(name = "ticket_id")
    private UUID value;

    private TicketId(UUID value) {
        if (value == null) {
            throw new BusinessValidationException("Invalid ticket id");
        }
        this.value = value;
    }

    public static TicketId of(UUID value) {
        return new TicketId(value);
    }

    public static TicketId generate() {
        return new TicketId(UUID.randomUUID());
    }

    UUID unwrap() {
        return value;
    }

    public boolean sameAs(TicketId other) {
        return other != null && this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "TicketId[" + value + "]";
    }
}

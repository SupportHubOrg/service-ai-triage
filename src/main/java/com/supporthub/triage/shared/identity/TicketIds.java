package com.supporthub.triage.shared.identity;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Sanctioned conversion of {@link TicketId} to its raw wire/JSON form. {@code TicketId} is mapped as
 * an {@code @Embeddable} identifier, so no JPA {@code AttributeConverter} is needed.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TicketIds {

    public static UUID toRaw(TicketId id) {
        return id == null ? null : id.unwrap();
    }
}

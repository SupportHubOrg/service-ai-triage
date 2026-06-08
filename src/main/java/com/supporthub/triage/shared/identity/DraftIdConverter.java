package com.supporthub.triage.shared.identity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;

/**
 * Single sanctioned conversion between the value object and its raw wire/column form. The static
 * {@code toRaw} is the one exit point (used by integration events / external wire formats); the JPA
 * {@link jakarta.persistence.AttributeConverter} methods delegate to it, so the unwrap lives once.
 */
@Converter(autoApply = true)
public class DraftIdConverter implements AttributeConverter<DraftId, UUID> {

    /** Sanctioned exit point for crossing into integration events / external wire formats. */
    public static UUID toRaw(DraftId id) {
        if (id == null) {
            return null;
        }
        return id.unwrap();
    }

    @Override
    public UUID convertToDatabaseColumn(DraftId attribute) {
        return toRaw(attribute);
    }

    @Override
    public DraftId convertToEntityAttribute(UUID dbData) {
        if (dbData == null) {
            return null;
        }
        return DraftId.of(dbData);
    }
}

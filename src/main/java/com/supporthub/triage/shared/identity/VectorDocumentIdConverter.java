package com.supporthub.triage.shared.identity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;

/**
 * Single sanctioned conversion between the value object and its raw wire/column form. The static
 * {@code toRaw} is the one exit point (used by the external vector store document id); the JPA
 * {@link jakarta.persistence.AttributeConverter} methods delegate to it, so the unwrap lives once.
 */
@Converter(autoApply = true)
public class VectorDocumentIdConverter implements AttributeConverter<VectorDocumentId, UUID> {

    /** Sanctioned exit point for crossing into the external vector store / wire formats. */
    public static UUID toRaw(VectorDocumentId id) {
        if (id == null) {
            return null;
        }
        return id.unwrap();
    }

    @Override
    public UUID convertToDatabaseColumn(VectorDocumentId attribute) {
        return toRaw(attribute);
    }

    @Override
    public VectorDocumentId convertToEntityAttribute(UUID dbData) {
        if (dbData == null) {
            return null;
        }
        return VectorDocumentId.of(dbData);
    }
}

package com.supporthub.triage.ai.tags.application;

import com.supporthub.triage.shared.identity.TagId;

/**
 * A tag the suggester selected for a ticket, resolved back to our own identity from the model's
 * name-based choice.
 *
 * @param id   internal tag identity
 * @param name tag name, as it appears in the catalog
 */
public record SuggestedTag(
        TagId id,
        String name
) {
}

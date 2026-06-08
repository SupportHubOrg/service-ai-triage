package com.supporthub.triage.ai.tags.application;

import com.supporthub.triage.shared.identity.TagId;

/**
 * A tag the model is allowed to choose from. The candidates come from the organization tag
 * projection (the AI-eligible tags), so the model selects from a fixed catalog and never invents new
 * tags.
 *
 * @param id          internal tag identity, never sent to the model
 * @param name        human-readable tag name shown to the model and used to resolve the selection
 * @param description optional explanation of when the tag applies; may be {@code null} until tag
 *                    descriptions are carried on the tag contract
 */
public record CandidateTag(
        TagId id,
        String name,
        String description
) {
}

package com.supporthub.triage.ai;

import java.util.List;

/**
 * Result of tag suggestion: the tags the model selected from the catalog, resolved to our identity.
 * Empty when nothing matched.
 *
 * @param tags selected tags, without duplicates
 */
public record SuggestedTags(
        List<SuggestedTag> tags
) {
}

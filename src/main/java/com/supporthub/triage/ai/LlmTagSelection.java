package com.supporthub.triage.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/**
 * Structured shape the chat model is asked to return. Spring AI maps the model's JSON into this
 * record via {@code ChatClient...entity(LlmTagSelection.class)}, so no manual JSON parsing is done.
 *
 * <p>The model returns tag <em>names</em> (from the supplied catalog), not our internal ids; the
 * suggester resolves names back to {@link com.supporthub.triage.shared.identity.TagId} afterwards.
 */
public record LlmTagSelection(
        @JsonPropertyDescription("Tags selected from the provided catalog. Empty if none apply.")
        List<SelectedTag> tags
) {

    public record SelectedTag(
            @JsonPropertyDescription("Tag name copied exactly from the catalog.")
            String name,
            @JsonPropertyDescription("Short justification grounded in the conversation.")
            String reason
    ) {
    }
}

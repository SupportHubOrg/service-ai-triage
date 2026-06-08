package com.supporthub.triage.tag.application;

import com.supporthub.triage.shared.identity.TagId;

public record AiEligibleTag(
        TagId tagId,
        String name
) {
}

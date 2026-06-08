package com.supporthub.triage.messaging.event;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.UUID;

@IntegrationEvent("tag-ai-setting-updated")
public record TagAiSettingUpdatedPayload(
        UUID tagId,
        UUID organizationId,
        UUID updatedById,
        @JsonAlias("excludeFromAi")
        boolean exludeFromAi
) {
}

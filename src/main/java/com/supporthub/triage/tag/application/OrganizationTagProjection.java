package com.supporthub.triage.tag.application;

import com.supporthub.triage.messaging.event.TagAiSettingUpdatedPayload;
import com.supporthub.triage.messaging.event.TagCreatedPayload;
import com.supporthub.triage.messaging.event.TagDeletedPayload;
import com.supporthub.triage.shared.identity.OrganizationId;
import com.supporthub.triage.shared.identity.TagId;
import com.supporthub.triage.tag.persistence.OrganizationTagEntity;
import com.supporthub.triage.tag.persistence.OrganizationTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationTagProjection {

    private final OrganizationTagRepository repository;
    private final Clock clock;

    @Transactional
    public void project(TagCreatedPayload payload) {
        var now = Instant.now(clock);
        var tagId = TagId.of(payload.tagId());
        var organizationId = OrganizationId.of(payload.organizationId());
        var tag = repository.findById(tagId)
                .orElseGet(() -> OrganizationTagEntity.created(
                        tagId,
                        organizationId,
                        payload.tagName(),
                        now
                ));

        tag.applyCreated(organizationId, payload.tagName(), now);
        repository.save(tag);
    }

    @Transactional
    public void project(TagDeletedPayload payload) {
        var now = Instant.now(clock);
        var tagId = TagId.of(payload.tagId());
        var organizationId = OrganizationId.of(payload.organizationId());
        var tag = repository.findById(tagId)
                .orElseGet(() -> OrganizationTagEntity.placeholder(
                        tagId,
                        organizationId,
                        now
                ));

        tag.markDeleted(organizationId, now);
        repository.save(tag);
    }

    @Transactional
    public void project(TagAiSettingUpdatedPayload payload) {
        var now = Instant.now(clock);
        var tagId = TagId.of(payload.tagId());
        var organizationId = OrganizationId.of(payload.organizationId());
        var tag = repository.findById(tagId)
                .orElseGet(() -> OrganizationTagEntity.placeholder(
                        tagId,
                        organizationId,
                        now
                ));

        tag.updateAiAvailability(organizationId, payload.exludeFromAi(), now);
        repository.save(tag);
    }

    @Transactional(readOnly = true)
    public List<AiEligibleTag> findAiEligibleTags(OrganizationId organizationId) {
        return repository
                .findByOrganizationIdAndDeletedFalseAndExcludedFromAiFalseAndNameIsNotNullOrderByNameAsc(
                        organizationId
                )
                .stream()
                .map(tag -> new AiEligibleTag(tag.getTagId(), tag.getName()))
                .toList();
    }
}

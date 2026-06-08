package com.supporthub.triage.tag.persistence;

import com.supporthub.triage.shared.identity.OrganizationId;
import com.supporthub.triage.shared.identity.TagId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "organization_tag_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrganizationTagEntity {

    @EmbeddedId
    private TagId tagId;

    @Column(name = "organization_id", nullable = false)
    private OrganizationId organizationId;

    @Column(name = "name")
    private String name;

    @Column(name = "excluded_from_ai", nullable = false)
    private boolean excludedFromAi;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static OrganizationTagEntity created(TagId tagId,
                                                OrganizationId organizationId,
                                                String name,
                                                Instant now) {
        var tag = placeholder(tagId, organizationId, now);
        tag.applyCreated(organizationId, name, now);
        return tag;
    }

    public static OrganizationTagEntity placeholder(TagId tagId,
                                                   OrganizationId organizationId,
                                                   Instant now) {
        var tag = new OrganizationTagEntity();
        tag.tagId = tagId;
        tag.organizationId = organizationId;
        tag.createdAt = now;
        tag.updatedAt = now;
        return tag;
    }

    public void applyCreated(OrganizationId organizationId, String name, Instant now) {
        this.organizationId = organizationId;
        this.name = trimName(name);
        this.deleted = false;
        this.updatedAt = now;
    }

    public void updateAiAvailability(OrganizationId organizationId, boolean excludedFromAi, Instant now) {
        this.organizationId = organizationId;
        this.excludedFromAi = excludedFromAi;
        this.updatedAt = now;
    }

    public void markDeleted(OrganizationId organizationId, Instant now) {
        this.organizationId = organizationId;
        this.deleted = true;
        this.updatedAt = now;
    }

    private String trimName(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}

package com.supporthub.triage.knowledge.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "knowledge_article_tag_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnowledgeArticleTagEntity {

    @EmbeddedId
    private KnowledgeArticleTagId id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static KnowledgeArticleTagEntity of(UUID articleId, UUID tagId, UUID organizationId, Instant now) {
        var tag = new KnowledgeArticleTagEntity();
        tag.id = KnowledgeArticleTagId.of(articleId, tagId);
        tag.organizationId = organizationId;
        tag.createdAt = now;
        return tag;
    }
}

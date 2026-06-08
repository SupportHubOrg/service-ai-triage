package com.supporthub.triage.knowledge.persistence;

import com.supporthub.triage.shared.identity.ArticleId;
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
@Table(name = "knowledge_article_tag_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnowledgeArticleTagEntity {

    @EmbeddedId
    private KnowledgeArticleTagId id;

    @Column(name = "organization_id", nullable = false)
    private OrganizationId organizationId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static KnowledgeArticleTagEntity of(ArticleId articleId, TagId tagId, OrganizationId organizationId, Instant now) {
        var tag = new KnowledgeArticleTagEntity();
        tag.id = KnowledgeArticleTagId.of(articleId, tagId);
        tag.organizationId = organizationId;
        tag.createdAt = now;
        return tag;
    }
}

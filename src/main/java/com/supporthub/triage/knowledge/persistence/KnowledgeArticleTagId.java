package com.supporthub.triage.knowledge.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnowledgeArticleTagId implements Serializable {

    @Column(name = "article_id", nullable = false)
    private UUID articleId;

    @Column(name = "tag_id", nullable = false)
    private UUID tagId;

    public static KnowledgeArticleTagId of(UUID articleId, UUID tagId) {
        var id = new KnowledgeArticleTagId();
        id.articleId = articleId;
        id.tagId = tagId;
        return id;
    }
}

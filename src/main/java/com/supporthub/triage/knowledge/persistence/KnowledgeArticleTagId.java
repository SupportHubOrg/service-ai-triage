package com.supporthub.triage.knowledge.persistence;

import com.supporthub.triage.shared.identity.ArticleId;
import com.supporthub.triage.shared.identity.TagId;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnowledgeArticleTagId implements Serializable {

    @Embedded
    private ArticleId articleId;

    @Embedded
    private TagId tagId;

    public static KnowledgeArticleTagId of(ArticleId articleId, TagId tagId) {
        var id = new KnowledgeArticleTagId();
        id.articleId = articleId;
        id.tagId = tagId;
        return id;
    }
}

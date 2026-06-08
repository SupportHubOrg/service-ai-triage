package com.supporthub.triage.knowledge.persistence;

import com.supporthub.triage.shared.identity.ArticleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface KnowledgeArticleTagRepository extends JpaRepository<KnowledgeArticleTagEntity, KnowledgeArticleTagId> {

    List<KnowledgeArticleTagEntity> findByIdArticleId(ArticleId articleId);

    @Modifying
    @Query("delete from KnowledgeArticleTagEntity tag where tag.id.articleId = :articleId")
    void deleteByArticleId(ArticleId articleId);
}

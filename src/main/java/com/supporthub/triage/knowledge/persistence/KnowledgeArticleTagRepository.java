package com.supporthub.triage.knowledge.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface KnowledgeArticleTagRepository extends JpaRepository<KnowledgeArticleTagEntity, KnowledgeArticleTagId> {

    List<KnowledgeArticleTagEntity> findByIdArticleId(UUID articleId);

    @Modifying
    @Query("delete from KnowledgeArticleTagEntity tag where tag.id.articleId = :articleId")
    void deleteByArticleId(UUID articleId);
}

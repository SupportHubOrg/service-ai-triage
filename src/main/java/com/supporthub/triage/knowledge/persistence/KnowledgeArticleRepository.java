package com.supporthub.triage.knowledge.persistence;

import com.supporthub.triage.shared.identity.ArticleId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticleEntity, ArticleId> {
}

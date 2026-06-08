package com.supporthub.triage.knowledge.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticleEntity, UUID> {
}

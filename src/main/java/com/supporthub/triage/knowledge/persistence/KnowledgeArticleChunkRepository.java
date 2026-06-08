package com.supporthub.triage.knowledge.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface KnowledgeArticleChunkRepository extends JpaRepository<KnowledgeArticleChunkEntity, UUID> {

    boolean existsByArticleIdAndContentHash(UUID articleId, String contentHash);

    List<KnowledgeArticleChunkEntity> findByArticleIdOrderByChunkIndexAsc(UUID articleId);

    @Modifying
    @Query("delete from KnowledgeArticleChunkEntity chunk where chunk.articleId = :articleId")
    void deleteByArticleId(UUID articleId);

    @Query(
            value = """
                    select *
                    from knowledge_article_chunk_projection
                    where index_status in ('PENDING', 'FAILED')
                      and index_attempts < :maxAttempts
                    order by updated_at, chunk_id
                    limit :batchSize
                    for update skip locked
                    """,
            nativeQuery = true
    )
    List<KnowledgeArticleChunkEntity> findIndexableChunksForUpdate(@Param("batchSize") int batchSize,
                                                                   @Param("maxAttempts") int maxAttempts);

    @Modifying
    @Query("""
            update KnowledgeArticleChunkEntity chunk
            set chunk.indexStatus = com.supporthub.triage.knowledge.persistence.KnowledgeArticleChunkIndexStatus.FAILED,
                chunk.lastIndexError = 'Indexing was interrupted and marked failed for retry',
                chunk.updatedAt = :now
            where chunk.indexStatus = com.supporthub.triage.knowledge.persistence.KnowledgeArticleChunkIndexStatus.IN_PROGRESS
              and chunk.updatedAt < :cutoff
            """)
    int markStuckInProgressAsFailed(@Param("cutoff") Instant cutoff, @Param("now") Instant now);
}

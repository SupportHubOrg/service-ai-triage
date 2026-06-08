package com.supporthub.triage.knowledge.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "knowledge_article_chunk_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnowledgeArticleChunkEntity {

    @Id
    @Column(name = "chunk_id", nullable = false)
    private UUID chunkId;

    @Column(name = "article_id", nullable = false)
    private UUID articleId;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    @Column(name = "chunk_count", nullable = false)
    private int chunkCount;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "content_hash", nullable = false)
    private String contentHash;

    @Column(name = "vector_document_id", nullable = false)
    private UUID vectorDocumentId;

    @Column(name = "embedding_model", nullable = false)
    private String embeddingModel;

    @Column(name = "embedding_dimensions", nullable = false)
    private int embeddingDimensions;

    @Enumerated(EnumType.STRING)
    @Column(name = "index_status", nullable = false)
    private KnowledgeArticleChunkIndexStatus indexStatus;

    @Column(name = "index_attempts", nullable = false)
    private int indexAttempts;

    @Column(name = "last_index_error")
    private String lastIndexError;

    @Column(name = "indexed_at")
    private Instant indexedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static KnowledgeArticleChunkEntity pending(UUID chunkId,
                                                      UUID articleId,
                                                      UUID organizationId,
                                                      int chunkIndex,
                                                      int chunkCount,
                                                      String content,
                                                      String contentHash,
                                                      UUID vectorDocumentId,
                                                      String embeddingModel,
                                                      int embeddingDimensions,
                                                      Instant now) {
        var chunk = new KnowledgeArticleChunkEntity();
        chunk.chunkId = chunkId;
        chunk.articleId = articleId;
        chunk.organizationId = organizationId;
        chunk.chunkIndex = chunkIndex;
        chunk.chunkCount = chunkCount;
        chunk.content = content;
        chunk.contentHash = contentHash;
        chunk.vectorDocumentId = vectorDocumentId;
        chunk.embeddingModel = embeddingModel;
        chunk.embeddingDimensions = embeddingDimensions;
        chunk.indexStatus = KnowledgeArticleChunkIndexStatus.PENDING;
        chunk.indexAttempts = 0;
        chunk.createdAt = now;
        chunk.updatedAt = now;
        return chunk;
    }

    public void markIndexingStarted(Instant now) {
        indexStatus = KnowledgeArticleChunkIndexStatus.IN_PROGRESS;
        indexAttempts++;
        lastIndexError = null;
        updatedAt = now;
    }

    public void markIndexed(Instant now) {
        indexStatus = KnowledgeArticleChunkIndexStatus.INDEXED;
        indexedAt = now;
        lastIndexError = null;
        updatedAt = now;
    }

    public void markIndexingFailed(String errorMessage, Instant now) {
        indexStatus = KnowledgeArticleChunkIndexStatus.FAILED;
        lastIndexError = truncate(errorMessage);
        updatedAt = now;
    }

    public void markPending(Instant now) {
        indexStatus = KnowledgeArticleChunkIndexStatus.PENDING;
        lastIndexError = null;
        updatedAt = now;
    }

    private String truncate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }
}

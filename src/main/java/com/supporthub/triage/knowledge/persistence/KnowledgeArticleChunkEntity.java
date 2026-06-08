package com.supporthub.triage.knowledge.persistence;

import com.supporthub.triage.shared.identity.ArticleId;
import com.supporthub.triage.shared.identity.ChunkId;
import com.supporthub.triage.shared.identity.OrganizationId;
import com.supporthub.triage.shared.identity.VectorDocumentId;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "knowledge_article_chunk_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnowledgeArticleChunkEntity {

    @EmbeddedId
    private ChunkId chunkId;

    @Embedded
    private ArticleId articleId;

    @Column(name = "organization_id", nullable = false)
    private OrganizationId organizationId;

    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    @Column(name = "chunk_count", nullable = false)
    private int chunkCount;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "content_hash", nullable = false)
    private String contentHash;

    @Column(name = "vector_document_id", nullable = false)
    private VectorDocumentId vectorDocumentId;

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

    public static KnowledgeArticleChunkEntity pending(ChunkId chunkId,
                                                      ArticleId articleId,
                                                      OrganizationId organizationId,
                                                      int chunkIndex,
                                                      int chunkCount,
                                                      String content,
                                                      String contentHash,
                                                      VectorDocumentId vectorDocumentId,
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

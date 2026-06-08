package com.supporthub.triage.knowledge.application;

import com.supporthub.triage.knowledge.config.RagProperties;
import com.supporthub.triage.knowledge.persistence.KnowledgeArticleChunkEntity;
import com.supporthub.triage.knowledge.persistence.KnowledgeArticleChunkRepository;
import com.supporthub.triage.knowledge.persistence.KnowledgeArticleEntity;
import com.supporthub.triage.knowledge.persistence.KnowledgeArticleRepository;
import com.supporthub.triage.knowledge.persistence.KnowledgeArticleTagEntity;
import com.supporthub.triage.knowledge.persistence.KnowledgeArticleTagRepository;
import com.supporthub.triage.messaging.event.KnowledgeArticleApprovedPayload;
import com.supporthub.triage.messaging.inbox.core.context.InboxEventContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KnowledgeArticleProjection {

    private static final String CHUNK_ID_PREFIX = "knowledge-article-chunk";

    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgeArticleChunkRepository chunkRepository;
    private final KnowledgeArticleTagRepository tagRepository;
    private final KnowledgeArticleChunker chunker;
    private final ContentHash contentHash;
    private final RagProperties ragProperties;
    private final Clock clock;

    @Transactional
    public void project(KnowledgeArticleApprovedPayload payload, InboxEventContext context) {
        validate(payload);

        var now = Instant.now(clock);
        var approvedAt = context.timestamp();
        var normalizedContent = payload.articleContent().trim();
        var articleHash = contentHash.sha256(normalizedContent);
        var chunks = chunker.chunk(normalizedContent);

        var article = articleRepository.findById(payload.articleId())
                .orElseGet(() -> KnowledgeArticleEntity.approved(
                        payload.articleId(),
                        payload.organizationId(),
                        payload.draftId(),
                        payload.sourceTicketId(),
                        payload.publishedById(),
                        payload.title(),
                        normalizedContent,
                        articleHash,
                        chunks.size(),
                        approvedAt,
                        now
                ));

        if (article.hasContentHash(articleHash)
                && chunkRepository.existsByArticleIdAndContentHash(payload.articleId(), articleHash)
                && tagsAlreadyProjected(payload)) {
            return;
        }

        article.applyApproved(
                payload.organizationId(),
                payload.draftId(),
                payload.sourceTicketId(),
                payload.publishedById(),
                payload.title(),
                normalizedContent,
                articleHash,
                chunks.size(),
                approvedAt,
                now
        );
        articleRepository.save(article);
        chunkRepository.deleteByArticleId(payload.articleId());
        chunkRepository.saveAll(toEntities(payload, articleHash, chunks, now));
        tagRepository.deleteByArticleId(payload.articleId());
        tagRepository.saveAll(toTagEntities(payload, now));
    }

    @Transactional(readOnly = true)
    public List<KnowledgeArticleChunkEntity> findChunks(UUID articleId) {
        return chunkRepository.findByArticleIdOrderByChunkIndexAsc(articleId);
    }

    @Transactional(readOnly = true)
    public List<UUID> findTagIds(UUID articleId) {
        return tagRepository.findByIdArticleId(articleId)
                .stream()
                .map(tag -> tag.getId().getTagId())
                .toList();
    }

    private void validate(KnowledgeArticleApprovedPayload payload) {
        require(payload.articleId(), "Article id is required");
        require(payload.organizationId(), "Organization id is required");
        require(payload.publishedById(), "Published by id is required");
        require(payload.title(), "Article title is required");
        require(payload.articleContent(), "Article content is required");
    }

    private void require(UUID value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private void require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private List<KnowledgeArticleChunkEntity> toEntities(KnowledgeArticleApprovedPayload payload,
                                                         String articleHash,
                                                         List<KnowledgeArticleChunk> chunks,
                                                         Instant now) {
        return chunks.stream()
                .map(chunk -> toEntity(payload, articleHash, chunks.size(), chunk, now))
                .toList();
    }

    private KnowledgeArticleChunkEntity toEntity(KnowledgeArticleApprovedPayload payload,
                                                 String articleHash,
                                                 int chunkCount,
                                                 KnowledgeArticleChunk chunk,
                                                 Instant now) {
        var chunkHash = contentHash.sha256(chunk.content());
        var chunkId = stableChunkId(payload.articleId(), chunk.index(), articleHash);

        return KnowledgeArticleChunkEntity.pending(
                chunkId,
                payload.articleId(),
                payload.organizationId(),
                chunk.index(),
                chunkCount,
                chunk.content(),
                chunkHash,
                chunkId,
                ragProperties.embedding().model(),
                ragProperties.embedding().dimensions(),
                now
        );
    }

    private UUID stableChunkId(UUID articleId, int chunkIndex, String articleHash) {
        var value = "%s:%s:%d:%s".formatted(CHUNK_ID_PREFIX, articleId, chunkIndex, articleHash);
        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));
    }

    private boolean tagsAlreadyProjected(KnowledgeArticleApprovedPayload payload) {
        var currentTags = findTagIds(payload.articleId());
        return currentTags.size() == tagIds(payload).size() && currentTags.containsAll(tagIds(payload));
    }

    private List<KnowledgeArticleTagEntity> toTagEntities(KnowledgeArticleApprovedPayload payload, Instant now) {
        return tagIds(payload)
                .stream()
                .map(tagId -> KnowledgeArticleTagEntity.of(payload.articleId(), tagId, payload.organizationId(), now))
                .toList();
    }

    private List<UUID> tagIds(KnowledgeArticleApprovedPayload payload) {
        if (payload.tags() == null) {
            return List.of();
        }
        return payload.tags()
                .stream()
                .filter(tagId -> tagId != null)
                .distinct()
                .toList();
    }
}

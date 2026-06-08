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
import com.supporthub.triage.shared.identity.ArticleId;
import com.supporthub.triage.shared.identity.ChunkId;
import com.supporthub.triage.shared.identity.DraftId;
import com.supporthub.triage.shared.identity.OrganizationId;
import com.supporthub.triage.shared.identity.TagId;
import com.supporthub.triage.shared.identity.TicketId;
import com.supporthub.triage.shared.identity.UserId;
import com.supporthub.triage.shared.identity.VectorDocumentId;
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

        var articleId = ArticleId.of(payload.articleId());
        var organizationId = OrganizationId.of(payload.organizationId());
        var now = Instant.now(clock);
        var approvedAt = context.timestamp();
        var normalizedContent = payload.articleContent().trim();
        var articleHash = contentHash.sha256(normalizedContent);
        var chunks = chunker.chunk(normalizedContent);

        var article = articleRepository.findById(articleId)
                .orElseGet(() -> KnowledgeArticleEntity.approved(
                        articleId,
                        organizationId,
                        draftId(payload),
                        sourceTicketId(payload),
                        UserId.of(payload.publishedById()),
                        payload.title(),
                        normalizedContent,
                        articleHash,
                        chunks.size(),
                        approvedAt,
                        now
                ));

        if (article.hasContentHash(articleHash)
                && chunkRepository.existsByArticleIdAndContentHash(articleId, articleHash)
                && tagsAlreadyProjected(payload, articleId)) {
            return;
        }

        article.applyApproved(
                organizationId,
                draftId(payload),
                sourceTicketId(payload),
                UserId.of(payload.publishedById()),
                payload.title(),
                normalizedContent,
                articleHash,
                chunks.size(),
                approvedAt,
                now
        );
        articleRepository.save(article);
        chunkRepository.deleteByArticleId(articleId);
        chunkRepository.saveAll(toEntities(articleId, organizationId, articleHash, chunks, now));
        tagRepository.deleteByArticleId(articleId);
        tagRepository.saveAll(toTagEntities(payload, articleId, organizationId, now));
    }

    @Transactional(readOnly = true)
    public List<KnowledgeArticleChunkEntity> findChunks(ArticleId articleId) {
        return chunkRepository.findByArticleIdOrderByChunkIndexAsc(articleId);
    }

    @Transactional(readOnly = true)
    public List<TagId> findTagIds(ArticleId articleId) {
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

    private DraftId draftId(KnowledgeArticleApprovedPayload payload) {
        return payload.draftId() == null ? null : DraftId.of(payload.draftId());
    }

    private TicketId sourceTicketId(KnowledgeArticleApprovedPayload payload) {
        return payload.sourceTicketId() == null ? null : TicketId.of(payload.sourceTicketId());
    }

    private List<KnowledgeArticleChunkEntity> toEntities(ArticleId articleId,
                                                         OrganizationId organizationId,
                                                         String articleHash,
                                                         List<KnowledgeArticleChunk> chunks,
                                                         Instant now) {
        return chunks.stream()
                .map(chunk -> toEntity(articleId, organizationId, articleHash, chunks.size(), chunk, now))
                .toList();
    }

    private KnowledgeArticleChunkEntity toEntity(ArticleId articleId,
                                                 OrganizationId organizationId,
                                                 String articleHash,
                                                 int chunkCount,
                                                 KnowledgeArticleChunk chunk,
                                                 Instant now) {
        var chunkHash = contentHash.sha256(chunk.content());
        var stableId = stableChunkUuid(articleId, chunk.index(), articleHash);

        return KnowledgeArticleChunkEntity.pending(
                ChunkId.of(stableId),
                articleId,
                organizationId,
                chunk.index(),
                chunkCount,
                chunk.content(),
                chunkHash,
                VectorDocumentId.of(stableId),
                ragProperties.embedding().model(),
                ragProperties.embedding().dimensions(),
                now
        );
    }

    private UUID stableChunkUuid(ArticleId articleId, int chunkIndex, String articleHash) {
        var value = "%s:%s:%d:%s".formatted(CHUNK_ID_PREFIX, articleId, chunkIndex, articleHash);
        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));
    }

    private boolean tagsAlreadyProjected(KnowledgeArticleApprovedPayload payload, ArticleId articleId) {
        var currentTags = findTagIds(articleId);
        var expectedTags = tagIds(payload);
        return currentTags.size() == expectedTags.size() && currentTags.containsAll(expectedTags);
    }

    private List<KnowledgeArticleTagEntity> toTagEntities(KnowledgeArticleApprovedPayload payload,
                                                          ArticleId articleId,
                                                          OrganizationId organizationId,
                                                          Instant now) {
        return tagIds(payload)
                .stream()
                .map(tagId -> KnowledgeArticleTagEntity.of(articleId, tagId, organizationId, now))
                .toList();
    }

    private List<TagId> tagIds(KnowledgeArticleApprovedPayload payload) {
        if (payload.tags() == null) {
            return List.of();
        }
        return payload.tags()
                .stream()
                .filter(tagId -> tagId != null)
                .distinct()
                .map(TagId::of)
                .toList();
    }
}

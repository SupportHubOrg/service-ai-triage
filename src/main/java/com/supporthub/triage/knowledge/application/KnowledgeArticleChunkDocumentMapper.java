package com.supporthub.triage.knowledge.application;

import com.supporthub.triage.knowledge.persistence.KnowledgeArticleChunkEntity;
import com.supporthub.triage.knowledge.persistence.KnowledgeArticleTagRepository;
import com.supporthub.triage.shared.identity.ArticleId;
import com.supporthub.triage.shared.identity.ArticleIds;
import com.supporthub.triage.shared.identity.ChunkIds;
import com.supporthub.triage.shared.identity.OrganizationIdConverter;
import com.supporthub.triage.shared.identity.TagIds;
import com.supporthub.triage.shared.identity.VectorDocumentIdConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KnowledgeArticleChunkDocumentMapper {

    private static final String DOCUMENT_TYPE = "knowledge_article";
    private static final String STATUS_APPROVED = "approved";

    private final KnowledgeArticleTagRepository tagRepository;

    public Document toDocument(KnowledgeArticleChunkEntity chunk) {
        return Document.builder()
                .id(VectorDocumentIdConverter.toRaw(chunk.getVectorDocumentId()).toString())
                .text(chunk.getContent())
                .metadata(metadata(chunk))
                .build();
    }

    private Map<String, Object> metadata(KnowledgeArticleChunkEntity chunk) {
        return Map.of(
                "organizationId", OrganizationIdConverter.toRaw(chunk.getOrganizationId()).toString(),
                "articleId", ArticleIds.toRaw(chunk.getArticleId()).toString(),
                "chunkId", ChunkIds.toRaw(chunk.getChunkId()).toString(),
                "chunkIndex", chunk.getChunkIndex(),
                "chunkCount", chunk.getChunkCount(),
                "docType", DOCUMENT_TYPE,
                "status", STATUS_APPROVED,
                "tagIds", tagIds(chunk.getArticleId()),
                "embeddingModel", chunk.getEmbeddingModel(),
                "embeddingDimensions", chunk.getEmbeddingDimensions()
        );
    }

    private String tagIds(ArticleId articleId) {
        return tagRepository.findByIdArticleId(articleId)
                .stream()
                .map(tag -> TagIds.toRaw(tag.getId().getTagId()).toString())
                .sorted()
                .collect(Collectors.joining(","));
    }
}

package com.supporthub.triage.knowledge.application;

import com.supporthub.triage.knowledge.persistence.KnowledgeArticleChunkEntity;
import com.supporthub.triage.knowledge.persistence.KnowledgeArticleTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KnowledgeArticleChunkDocumentMapper {

    private static final String DOCUMENT_TYPE = "knowledge_article";
    private static final String STATUS_APPROVED = "approved";

    private final KnowledgeArticleTagRepository tagRepository;

    public Document toDocument(KnowledgeArticleChunkEntity chunk) {
        return Document.builder()
                .id(chunk.getVectorDocumentId().toString())
                .text(chunk.getContent())
                .metadata(metadata(chunk))
                .build();
    }

    private Map<String, Object> metadata(KnowledgeArticleChunkEntity chunk) {
        return Map.of(
                "organizationId", chunk.getOrganizationId().toString(),
                "articleId", chunk.getArticleId().toString(),
                "chunkId", chunk.getChunkId().toString(),
                "chunkIndex", chunk.getChunkIndex(),
                "chunkCount", chunk.getChunkCount(),
                "docType", DOCUMENT_TYPE,
                "status", STATUS_APPROVED,
                "tagIds", tagIds(chunk.getArticleId()),
                "embeddingModel", chunk.getEmbeddingModel(),
                "embeddingDimensions", chunk.getEmbeddingDimensions()
        );
    }

    private String tagIds(UUID articleId) {
        return tagRepository.findByIdArticleId(articleId)
                .stream()
                .map(tag -> tag.getId().getTagId().toString())
                .sorted()
                .collect(Collectors.joining(","));
    }
}

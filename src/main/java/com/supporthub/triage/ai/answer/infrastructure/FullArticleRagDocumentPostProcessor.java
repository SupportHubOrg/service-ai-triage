package com.supporthub.triage.ai.answer.infrastructure;

import com.supporthub.triage.knowledge.config.RagProperties;
import com.supporthub.triage.knowledge.persistence.KnowledgeArticleRepository;
import com.supporthub.triage.knowledge.persistence.KnowledgeArticleStatus;
import com.supporthub.triage.knowledge.persistence.KnowledgeArticleTagRepository;
import com.supporthub.triage.shared.identity.ArticleId;
import com.supporthub.triage.shared.identity.ArticleIds;
import com.supporthub.triage.shared.identity.TagId;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class FullArticleRagDocumentPostProcessor implements DocumentPostProcessor {

    static final String REQUIRED_TAG_IDS_CONTEXT_KEY = "supporthub_required_tag_ids";
    static final String ARTICLE_ID_METADATA_KEY = "articleId";

    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgeArticleTagRepository tagRepository;
    private final RagProperties ragProperties;

    @Override
    @Transactional(readOnly = true)
    public List<Document> process(Query query, List<Document> documents) {
        var requiredTags = requiredTags(query);
        return documents.stream()
                .map(this::articleId)
                .filter(Objects::nonNull)
                .distinct()
                .map(articleId -> articleDocument(articleId, requiredTags))
                .filter(Objects::nonNull)
                .limit(ragProperties.retrieval().maxArticles())
                .toList();
    }

    private List<TagId> requiredTags(Query query) {
        var value = query.context().get(REQUIRED_TAG_IDS_CONTEXT_KEY);
        if (!(value instanceof List<?> values)) {
            return List.of();
        }
        return values.stream()
                .filter(TagId.class::isInstance)
                .map(TagId.class::cast)
                .toList();
    }

    private ArticleId articleId(Document document) {
        var value = document.getMetadata().get(ARTICLE_ID_METADATA_KEY);
        if (value == null) {
            return null;
        }
        return ArticleId.of(UUID.fromString(value.toString()));
    }

    private Document articleDocument(ArticleId articleId, List<TagId> requiredTags) {
        return articleRepository.findById(articleId)
                .filter(article -> article.getStatus() == KnowledgeArticleStatus.APPROVED)
                .filter(article -> articleHasRequiredTags(article.getArticleId(), requiredTags))
                .map(article -> new Document(
                        ArticleIds.toRaw(article.getArticleId()).toString(),
                        """
                        Article: %s
                        Content:
                        %s
                        """.formatted(article.getTitle(), article.getArticleContent()),
                        metadata(article.getArticleId())
                ))
                .orElse(null);
    }

    private Map<String, Object> metadata(ArticleId articleId) {
        var metadata = new LinkedHashMap<String, Object>();
        metadata.put(ARTICLE_ID_METADATA_KEY, ArticleIds.toRaw(articleId).toString());
        return metadata;
    }

    private boolean articleHasRequiredTags(ArticleId articleId, List<TagId> requiredTags) {
        if (requiredTags == null || requiredTags.isEmpty()) {
            return true;
        }
        return tagRepository.findByIdArticleId(articleId)
                .stream()
                .map(tag -> tag.getId().getTagId())
                .anyMatch(requiredTags::contains);
    }
}

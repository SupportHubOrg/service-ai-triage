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
@Table(name = "knowledge_article_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnowledgeArticleEntity {

    @Id
    @Column(name = "article_id", nullable = false)
    private UUID articleId;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "draft_id")
    private UUID draftId;

    @Column(name = "source_ticket_id")
    private UUID sourceTicketId;

    @Column(name = "published_by_id", nullable = false)
    private UUID publishedById;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "article_content", nullable = false)
    private String articleContent;

    @Column(name = "content_hash", nullable = false)
    private String contentHash;

    @Column(name = "chunk_count", nullable = false)
    private int chunkCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private KnowledgeArticleStatus status;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static KnowledgeArticleEntity approved(UUID articleId,
                                                  UUID organizationId,
                                                  UUID draftId,
                                                  UUID sourceTicketId,
                                                  UUID publishedById,
                                                  String title,
                                                  String articleContent,
                                                  String contentHash,
                                                  int chunkCount,
                                                  Instant approvedAt,
                                                  Instant now) {
        var article = new KnowledgeArticleEntity();
        article.articleId = articleId;
        article.createdAt = now;
        article.applyApproved(
                organizationId,
                draftId,
                sourceTicketId,
                publishedById,
                title,
                articleContent,
                contentHash,
                chunkCount,
                approvedAt,
                now
        );
        return article;
    }

    public void applyApproved(UUID organizationId,
                              UUID draftId,
                              UUID sourceTicketId,
                              UUID publishedById,
                              String title,
                              String articleContent,
                              String contentHash,
                              int chunkCount,
                              Instant approvedAt,
                              Instant now) {
        this.organizationId = organizationId;
        this.draftId = draftId;
        this.sourceTicketId = sourceTicketId;
        this.publishedById = publishedById;
        this.title = trim(title);
        this.articleContent = articleContent.trim();
        this.contentHash = contentHash;
        this.chunkCount = chunkCount;
        this.status = KnowledgeArticleStatus.APPROVED;
        this.approvedAt = approvedAt;
        this.updatedAt = now;
    }

    public boolean hasContentHash(String value) {
        return contentHash != null && contentHash.equals(value);
    }

    private String trim(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}

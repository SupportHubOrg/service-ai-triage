package com.supporthub.triage.knowledge.persistence;

import com.supporthub.triage.shared.identity.ArticleId;
import com.supporthub.triage.shared.identity.DraftId;
import com.supporthub.triage.shared.identity.OrganizationId;
import com.supporthub.triage.shared.identity.TicketId;
import com.supporthub.triage.shared.identity.UserId;
import jakarta.persistence.AttributeOverride;
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
@Table(name = "knowledge_article_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnowledgeArticleEntity {

    @EmbeddedId
    private ArticleId articleId;

    @Column(name = "organization_id", nullable = false)
    private OrganizationId organizationId;

    @Column(name = "draft_id")
    private DraftId draftId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "source_ticket_id"))
    private TicketId sourceTicketId;

    @Column(name = "published_by_id", nullable = false)
    private UserId publishedById;

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

    public static KnowledgeArticleEntity approved(ArticleId articleId,
                                                  OrganizationId organizationId,
                                                  DraftId draftId,
                                                  TicketId sourceTicketId,
                                                  UserId publishedById,
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

    public void applyApproved(OrganizationId organizationId,
                              DraftId draftId,
                              TicketId sourceTicketId,
                              UserId publishedById,
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

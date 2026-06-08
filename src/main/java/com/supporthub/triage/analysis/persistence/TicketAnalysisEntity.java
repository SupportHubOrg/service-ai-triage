package com.supporthub.triage.analysis.persistence;

import com.supporthub.triage.shared.identity.AnalysisId;
import com.supporthub.triage.shared.identity.OrganizationId;
import com.supporthub.triage.shared.identity.TicketId;
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
@Table(name = "ticket_analysis")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketAnalysisEntity {

    @EmbeddedId
    private AnalysisId analysisId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "ticket_id"))
    private TicketId ticketId;

    @Column(name = "organization_id", nullable = false)
    private OrganizationId organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TicketAnalysisStatus status;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "locked_at")
    private Instant lockedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static TicketAnalysisEntity pending(AnalysisId analysisId,
                                               TicketId ticketId,
                                               OrganizationId organizationId,
                                               Instant now) {
        var analysis = new TicketAnalysisEntity();
        analysis.analysisId = analysisId;
        analysis.ticketId = ticketId;
        analysis.organizationId = organizationId;
        analysis.status = TicketAnalysisStatus.PENDING_TAG_ANALYSIS;
        analysis.attempts = 0;
        analysis.createdAt = now;
        analysis.updatedAt = now;
        return analysis;
    }

    public void markTagAnalysisStarted(Instant now) {
        status = TicketAnalysisStatus.TAG_ANALYSIS_IN_PROGRESS;
        attempts++;
        lockedAt = now;
        completedAt = null;
        lastError = null;
        updatedAt = now;
    }

    public void markAnswerGenerationPending(Instant now) {
        status = TicketAnalysisStatus.PENDING_ANSWER_GENERATION;
        attempts = 0;
        lockedAt = null;
        completedAt = null;
        lastError = null;
        updatedAt = now;
    }

    public void markAnswerGenerationStarted(Instant now) {
        status = TicketAnalysisStatus.ANSWER_GENERATION_IN_PROGRESS;
        attempts++;
        lockedAt = now;
        completedAt = null;
        lastError = null;
        updatedAt = now;
    }

    public void markPendingForRetry(String errorMessage, Instant now) {
        status = retryStatus();
        lockedAt = null;
        lastError = truncate(errorMessage);
        updatedAt = now;
    }

    public void markCompleted(Instant now) {
        status = TicketAnalysisStatus.COMPLETED;
        lockedAt = null;
        completedAt = now;
        lastError = null;
        updatedAt = now;
    }

    public void markFailed(String errorMessage, Instant now) {
        status = TicketAnalysisStatus.FAILED;
        lockedAt = null;
        completedAt = now;
        lastError = truncate(errorMessage);
        updatedAt = now;
    }

    public boolean canRetry(int maxAttempts) {
        return attempts < maxAttempts;
    }

    private TicketAnalysisStatus retryStatus() {
        if (status == TicketAnalysisStatus.ANSWER_GENERATION_IN_PROGRESS) {
            return TicketAnalysisStatus.PENDING_ANSWER_GENERATION;
        }
        return TicketAnalysisStatus.PENDING_TAG_ANALYSIS;
    }

    private String truncate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }
}

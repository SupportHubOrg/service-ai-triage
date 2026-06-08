package com.supporthub.triage.analysis.application;

import com.supporthub.triage.ai.tags.application.SuggestedTag;
import com.supporthub.triage.ai.tags.application.SuggestedTags;
import com.supporthub.triage.analysis.persistence.TicketAnalysisEntity;
import com.supporthub.triage.analysis.persistence.TicketAnalysisRepository;
import com.supporthub.triage.analysis.persistence.TicketAnalysisStatus;
import com.supporthub.triage.analysis.persistence.TicketAnalysisSuggestedTagEntity;
import com.supporthub.triage.analysis.persistence.TicketAnalysisSuggestedTagRepository;
import com.supporthub.triage.messaging.TriageEventPublisher;
import com.supporthub.triage.shared.identity.AnalysisId;
import com.supporthub.triage.shared.identity.OrganizationId;
import com.supporthub.triage.shared.identity.TicketId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketAnalysisJobStore {

    private final TicketAnalysisRepository repository;
    private final TicketAnalysisSuggestedTagRepository suggestedTagRepository;
    private final TriageEventPublisher eventPublisher;
    private final TicketAnalysisProperties properties;
    private final Clock clock;

    @Transactional
    public void requestAnalysis(TicketId ticketId, OrganizationId organizationId) {
        if (repository.existsByTicketId(ticketId)) {
            return;
        }
        var now = Instant.now(clock);
        repository.save(TicketAnalysisEntity.pending(
                AnalysisId.generate(),
                ticketId,
                organizationId,
                now
        ));
    }

    @Transactional
    public List<TicketAnalysisEntity> claimTagAnalysisBatch() {
        var now = Instant.now(clock);
        recoverStuckAnalyses(now);

        var analyses = repository.findPendingForUpdate(
                TicketAnalysisStatus.PENDING_TAG_ANALYSIS.name(),
                properties.batchSize(),
                properties.maxAttempts()
        );
        analyses.forEach(analysis -> analysis.markTagAnalysisStarted(now));
        return analyses;
    }

    @Transactional
    public List<TicketAnalysisEntity> claimAnswerGenerationBatch() {
        var now = Instant.now(clock);
        recoverStuckAnalyses(now);

        var analyses = repository.findPendingForUpdate(
                TicketAnalysisStatus.PENDING_ANSWER_GENERATION.name(),
                properties.batchSize(),
                properties.maxAttempts()
        );
        analyses.forEach(analysis -> analysis.markAnswerGenerationStarted(now));
        return analyses;
    }

    @Transactional
    public void saveSuggestedTagsAndQueueAnswer(AnalysisId analysisId, SuggestedTags suggestedTags) {
        var analysis = repository.getReferenceById(analysisId);
        suggestedTagRepository.deleteByAnalysisId(analysisId);
        suggestedTagRepository.saveAll(toEntities(analysisId, suggestedTags));
        analysis.markAnswerGenerationPending(Instant.now(clock));
    }

    @Transactional(readOnly = true)
    public SuggestedTags findSuggestedTags(AnalysisId analysisId) {
        return new SuggestedTags(suggestedTagRepository.findByIdAnalysisIdOrderByTagName(analysisId)
                .stream()
                .map(TicketAnalysisSuggestedTagEntity::toSuggestedTag)
                .toList());
    }

    @Transactional
    public void complete(TicketAiAnalysisResult result) {
        var analysis = repository.getReferenceById(result.analysisId());
        analysis.markCompleted(Instant.now(clock));
        eventPublisher.publishAiAnalysisCompleted(result);
    }

    @Transactional
    public void failTerminal(TicketAiAnalysisFailure failure) {
        var analysis = repository.getReferenceById(failure.analysisId());
        analysis.markFailed(failure.failureMessage(), Instant.now(clock));
        eventPublisher.publishAiAnalysisFailed(failure);
    }

    @Transactional
    public void releaseForRetry(AnalysisId analysisId, String errorMessage) {
        var analysis = repository.getReferenceById(analysisId);
        analysis.markPendingForRetry(errorMessage, Instant.now(clock));
    }

    public boolean canRetry(TicketAnalysisEntity analysis) {
        return analysis.canRetry(properties.maxAttempts());
    }

    private void recoverStuckAnalyses(Instant now) {
        var stuckTimeout = properties.stuckTimeout();
        if (stuckTimeout == null || stuckTimeout.isNegative() || stuckTimeout.isZero()) {
            return;
        }
        var cutoff = now.minus(stuckTimeout);
        repository.markStuckStageAsPending(
                TicketAnalysisStatus.TAG_ANALYSIS_IN_PROGRESS.name(),
                TicketAnalysisStatus.PENDING_TAG_ANALYSIS.name(),
                cutoff,
                now
        );
        repository.markStuckStageAsPending(
                TicketAnalysisStatus.ANSWER_GENERATION_IN_PROGRESS.name(),
                TicketAnalysisStatus.PENDING_ANSWER_GENERATION.name(),
                cutoff,
                now
        );
    }

    private List<TicketAnalysisSuggestedTagEntity> toEntities(AnalysisId analysisId, SuggestedTags suggestedTags) {
        return suggestedTags.tags()
                .stream()
                .map(tag -> toEntity(analysisId, tag))
                .toList();
    }

    private TicketAnalysisSuggestedTagEntity toEntity(AnalysisId analysisId, SuggestedTag tag) {
        return TicketAnalysisSuggestedTagEntity.selected(analysisId, tag);
    }
}

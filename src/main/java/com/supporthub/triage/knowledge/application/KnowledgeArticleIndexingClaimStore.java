package com.supporthub.triage.knowledge.application;

import com.supporthub.triage.knowledge.config.RagProperties;
import com.supporthub.triage.knowledge.persistence.KnowledgeArticleChunkEntity;
import com.supporthub.triage.knowledge.persistence.KnowledgeArticleChunkRepository;
import com.supporthub.triage.shared.identity.ChunkId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeArticleIndexingClaimStore {

    private final KnowledgeArticleChunkRepository chunkRepository;
    private final RagProperties ragProperties;
    private final Clock clock;

    @Transactional
    public List<KnowledgeArticleChunkEntity> claimBatch() {
        var now = Instant.now(clock);
        recoverStuckChunks(now);

        var chunks = chunkRepository.findIndexableChunksForUpdate(
                ragProperties.indexing().batchSize(),
                ragProperties.indexing().maxAttempts()
        );
        chunks.forEach(chunk -> chunk.markIndexingStarted(now));

        return chunks;
    }

    @Transactional
    public void markIndexed(Collection<ChunkId> chunkIds) {
        var now = Instant.now(clock);
        chunkRepository.findAllById(chunkIds)
                .forEach(chunk -> chunk.markIndexed(now));
    }

    @Transactional
    public void markFailed(Collection<ChunkId> chunkIds, String errorMessage) {
        var now = Instant.now(clock);
        chunkRepository.findAllById(chunkIds)
                .forEach(chunk -> chunk.markIndexingFailed(errorMessage, now));
    }

    private void recoverStuckChunks(Instant now) {
        var stuckTimeout = ragProperties.indexing().stuckTimeout();
        if (stuckTimeout == null || stuckTimeout.isNegative() || stuckTimeout.isZero()) {
            return;
        }
        chunkRepository.markStuckInProgressAsFailed(now.minus(stuckTimeout), now);
    }
}

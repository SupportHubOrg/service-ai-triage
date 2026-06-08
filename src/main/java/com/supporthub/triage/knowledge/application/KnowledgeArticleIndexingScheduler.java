package com.supporthub.triage.knowledge.application;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(KnowledgeArticleEmbeddingIndexer.class)
@ConditionalOnProperty(name = "application.rag.indexing.enabled", havingValue = "true")
@RequiredArgsConstructor
public class KnowledgeArticleIndexingScheduler {

    private final KnowledgeArticleEmbeddingIndexer indexer;

    @Scheduled(fixedDelayString = "${application.rag.indexing.poll-delay-ms}")
    @SchedulerLock(
            name = "ai-triage-knowledge-article-indexer",
            lockAtMostFor = "${application.rag.indexing.lock-at-most-for}",
            lockAtLeastFor = "${application.rag.indexing.lock-at-least-for}"
    )
    public void indexPendingChunks() {
        indexer.indexPendingChunks();
    }
}

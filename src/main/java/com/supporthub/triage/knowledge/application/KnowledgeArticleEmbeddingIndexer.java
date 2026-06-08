package com.supporthub.triage.knowledge.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeArticleEmbeddingIndexer {

    private final KnowledgeArticleIndexingClaimStore claimStore;
    private final KnowledgeArticleChunkDocumentMapper documentMapper;
    private final VectorStore vectorStore;

    public int indexPendingChunks() {
        var chunks = claimStore.claimBatch();
        if (chunks.isEmpty()) {
            return 0;
        }

        var chunkIds = chunks.stream()
                .map(chunk -> chunk.getChunkId())
                .toList();
        try {
            var documents = chunks.stream()
                    .map(documentMapper::toDocument)
                    .toList();

            vectorStore.add(documents);
            claimStore.markIndexed(chunkIds);
            log.info("Indexed knowledge article chunks. count={}", chunks.size());
            return chunks.size();
        } catch (RuntimeException exception) {
            claimStore.markFailed(chunkIds, exception.getMessage());
            throw exception;
        }
    }
}

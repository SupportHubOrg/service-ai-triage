package com.supporthub.triage.knowledge.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "application.rag")
public record RagProperties(
        @Valid Embedding embedding,
        @Valid Chunking chunking,
        @Valid Indexing indexing,
        @Valid Retrieval retrieval
) {

    public record Embedding(
            @NotBlank String model,
            @Min(1) int dimensions
    ) {
    }

    public record Chunking(
            @Min(256) int maxChars,
            @Min(0) int overlapChars
    ) {
        public Chunking {
            if (overlapChars >= maxChars) {
                throw new IllegalArgumentException("RAG chunk overlap must be smaller than max chars");
            }
        }
    }

    public record Indexing(
            boolean enabled,
            @Min(1) int batchSize,
            @Min(1) int maxAttempts,
            long pollDelayMs,
            Duration lockAtMostFor,
            Duration lockAtLeastFor,
            Duration stuckTimeout
    ) {
    }

    public record Retrieval(
            @Min(1) int topK,
            @Min(1) int maxArticles,
            @Min(1) int maxExpandedQueries,
            double similarityThreshold
    ) {
    }
}

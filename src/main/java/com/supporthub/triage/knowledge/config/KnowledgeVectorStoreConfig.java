package com.supporthub.triage.knowledge.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openaisdk.OpenAiSdkEmbeddingModel;
import org.springframework.ai.openaisdk.OpenAiSdkEmbeddingOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

@Configuration
@RequiredArgsConstructor
public class KnowledgeVectorStoreConfig {

    private static final String VECTOR_TABLE_NAME = "vector_store";

    private final RagProperties ragProperties;

    @Bean
    EmbeddingModel knowledgeEmbeddingModel(@Value("${spring.ai.openai-sdk.api-key:}") String apiKey) {
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("OPENAI_API_KEY is required for knowledge article embeddings");
        }
        return new OpenAiSdkEmbeddingModel(OpenAiSdkEmbeddingOptions.builder()
                .apiKey(apiKey)
                .model(ragProperties.embedding().model())
                .dimensions(ragProperties.embedding().dimensions())
                .build());
    }

    @Bean
    @ConditionalOnBean(EmbeddingModel.class)
    @ConditionalOnMissingBean(VectorStore.class)
    VectorStore knowledgeVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .vectorTableName(VECTOR_TABLE_NAME)
                .dimensions(ragProperties.embedding().dimensions())
                .initializeSchema(false)
                .build();
    }
}

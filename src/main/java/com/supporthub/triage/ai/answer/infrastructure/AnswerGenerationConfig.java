package com.supporthub.triage.ai.answer.infrastructure;

import com.supporthub.triage.ai.answer.application.AnswerGenerationException;
import com.supporthub.triage.ai.common.CredentialScrubbingAdvisor;
import com.supporthub.triage.ai.common.SafeAiLoggingAdvisor;
import com.supporthub.triage.ai.common.SecretSanitizer;
import com.supporthub.triage.knowledge.config.RagProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openaisdk.OpenAiSdkChatModel;
import org.springframework.ai.openaisdk.OpenAiSdkChatOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.List;

@Configuration
@ConditionalOnProperty(prefix = "application.ai.answer-generation", name = "provider", havingValue = "openai")
class AnswerGenerationConfig {

    private static final String TARGET_SEARCH_SYSTEM = "knowledge-base vector store";

    private static final PromptTemplate QUERY_REWRITE_PROMPT = new PromptTemplate("""
            Rewrite this support ticket into one concise standalone search query for {target}.

            Preserve product names, error messages, protocols, codes and affected components.
            Use only facts present in the ticket.

            Ticket:
            {query}

            Search query:
            """);

    private static final PromptTemplate QUERY_EXPANSION_PROMPT = new PromptTemplate("""
            Generate {number} alternative knowledge-base search queries for the support ticket.

            Each query must use close synonyms or alternate support wording, without adding facts.
            Return one query per line and no explanations.

            Original query:
            {query}

            Query variants:
            """);

    private static final PromptTemplate ANSWER_AUGMENTATION_PROMPT = new PromptTemplate("""
            Approved knowledge-base context is below.

            ---------------------
            {context}
            ---------------------

            Use this context and the ticket data to draft the operator answer.
            If the context is insufficient, say what is missing and set a low confidence score.

            Ticket:
            {query}
            """);

    @Bean
    OpenAiSdkChatModel answerGenerationChatModel(AnswerGenerationProperties properties,
                                                 @Value("${spring.ai.openai-sdk.api-key:}") String apiKey) {
        if (!StringUtils.hasText(apiKey)) {
            throw new AnswerGenerationException("OPENAI_API_KEY is required when answer generation provider is openai");
        }
        return OpenAiSdkChatModel.builder()
                .options(OpenAiSdkChatOptions.builder()
                        .apiKey(apiKey)
                        .model(properties.model())
                        .store(false)
                        .build())
                .build();
    }

    @Bean
    @AnswerGenerationChatClient
    ChatClient answerGenerationChatClient(OpenAiSdkChatModel answerGenerationChatModel,
                                          SecretSanitizer secretSanitizer,
                                          AnswerGenerationProperties properties,
                                          RetrievalAugmentationAdvisor ticketAnswerRagAdvisor) {
        return ChatClient.builder(answerGenerationChatModel)
                .defaultAdvisors(advisors(secretSanitizer, properties, ticketAnswerRagAdvisor))
                .build();
    }

    @Bean
    RetrievalAugmentationAdvisor ticketAnswerRagAdvisor(OpenAiSdkChatModel answerGenerationChatModel,
                                                        VectorStore vectorStore,
                                                        RagProperties ragProperties,
                                                        FullArticleRagDocumentPostProcessor documentPostProcessor) {
        var chatClientBuilder = ChatClient.builder(answerGenerationChatModel);
        return RetrievalAugmentationAdvisor.builder()
                .queryTransformers(RewriteQueryTransformer.builder()
                        .chatClientBuilder(chatClientBuilder)
                        .promptTemplate(QUERY_REWRITE_PROMPT)
                        .targetSearchSystem(TARGET_SEARCH_SYSTEM)
                        .build())
                .queryExpander(MultiQueryExpander.builder()
                        .chatClientBuilder(chatClientBuilder)
                        .promptTemplate(QUERY_EXPANSION_PROMPT)
                        .numberOfQueries(ragProperties.retrieval().maxExpandedQueries())
                        .includeOriginal(true)
                        .build())
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .topK(ragProperties.retrieval().topK())
                        .similarityThreshold(ragProperties.retrieval().similarityThreshold())
                        .build())
                .documentPostProcessors(documentPostProcessor)
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .promptTemplate(ANSWER_AUGMENTATION_PROMPT)
                        .allowEmptyContext(true)
                        .build())
                .build();
    }

    private List<Advisor> advisors(SecretSanitizer secretSanitizer,
                                   AnswerGenerationProperties properties,
                                   RetrievalAugmentationAdvisor ragAdvisor) {
        return List.of(
                new CredentialScrubbingAdvisor(secretSanitizer),
                new SafeAiLoggingAdvisor(properties),
                ragAdvisor
        );
    }
}

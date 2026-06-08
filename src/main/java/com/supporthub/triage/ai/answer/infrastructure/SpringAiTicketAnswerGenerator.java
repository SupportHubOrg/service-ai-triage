package com.supporthub.triage.ai.answer.infrastructure;

import com.supporthub.triage.ai.answer.application.AnswerGenerationException;
import com.supporthub.triage.ai.answer.application.GeneratedTicketAnswer;
import com.supporthub.triage.ai.answer.application.TicketAnswerGenerationContext;
import com.supporthub.triage.ai.answer.application.TicketAnswerGenerator;
import com.supporthub.triage.ai.common.SafeAiLoggingAdvisor;
import com.supporthub.triage.ai.tags.application.SuggestedTag;
import com.supporthub.triage.ai.tags.application.SuggestedTags;
import com.supporthub.triage.shared.identity.ArticleId;
import com.supporthub.triage.shared.identity.OrganizationIdConverter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.openaisdk.OpenAiSdkChatOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "application.ai.answer-generation", name = "provider", havingValue = "openai")
class SpringAiTicketAnswerGenerator implements TicketAnswerGenerator {

    private static final String OPERATION = "ticket-answer-generation";
    private static final String DOCUMENT_TYPE = "knowledge_article";
    private static final String STATUS_APPROVED = "approved";

    private static final String SYSTEM_PROMPT = """
            You draft a concise support answer for an operator.

            Rules:
            - Use the provided ticket and approved knowledge-base articles only.
            - Do not invent facts, passwords, account state or system state.
            - If the knowledge context is insufficient, say what is missing and set low confidence.
            - The answer must be directly usable by a support operator.
            - Return confidenceScore from 0.0 to 1.0 based on how strongly the knowledge supports the answer.
            """;

    private static final String USER_PROMPT_TEMPLATE = """
            Ticket subject:
            {subject}

            Ticket description:
            {description}

            Suggested tags:
            {tags}

            Ticket conversation:
            {conversation}

            Draft the operator answer.
            """;

    private final ChatClient chatClient;
    private final AnswerGenerationProperties properties;

    SpringAiTicketAnswerGenerator(@AnswerGenerationChatClient ChatClient chatClient,
                                  AnswerGenerationProperties properties) {
        this.chatClient = chatClient;
        this.properties = properties;
    }

    @Override
    public GeneratedTicketAnswer generate(TicketAnswerGenerationContext context) {
        try {
            var response = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(user -> user.text(USER_PROMPT_TEMPLATE)
                            .param("subject", value(context.ticket().subject()))
                            .param("description", value(context.ticket().description()))
                            .param("tags", tags(context.suggestedTags()))
                            .param("conversation", conversation(context)))
                    .options(chatOptions())
                    .advisors(advisor -> advisor.params(advisorContext(context)))
                    .call()
                    .responseEntity(LlmTicketAnswer.class);

            return toAnswer(response.entity(), usedArticleIds(response.response()));
        } catch (RuntimeException exception) {
            throw new AnswerGenerationException("Failed to generate ticket answer", exception);
        }
    }

    private OpenAiSdkChatOptions chatOptions() {
        return OpenAiSdkChatOptions.builder()
                .model(properties.model())
                .maxCompletionTokens(properties.maxOutputTokens())
                .store(false)
                .build();
    }

    private Map<String, Object> advisorContext(TicketAnswerGenerationContext context) {
        return Map.of(
                SafeAiLoggingAdvisor.CONTEXT_OPERATION, OPERATION,
                SafeAiLoggingAdvisor.CONTEXT_TICKET_ID, context.ticket().ticketId(),
                SafeAiLoggingAdvisor.CONTEXT_ORGANIZATION_ID, context.ticket().organizationId(),
                SafeAiLoggingAdvisor.CONTEXT_MODEL, properties.model(),
                VectorStoreDocumentRetriever.FILTER_EXPRESSION, filterExpression(context),
                FullArticleRagDocumentPostProcessor.REQUIRED_TAG_IDS_CONTEXT_KEY, context.retrievalTagIds()
        );
    }

    private String filterExpression(TicketAnswerGenerationContext context) {
        return "organizationId == '%s' && docType == '%s' && status == '%s'".formatted(
                OrganizationIdConverter.toRaw(context.ticket().organizationId()),
                DOCUMENT_TYPE,
                STATUS_APPROVED
        );
    }

    private String tags(SuggestedTags suggestedTags) {
        if (suggestedTags.tags().isEmpty()) {
            return "none";
        }
        return suggestedTags.tags()
                .stream()
                .map(SuggestedTag::name)
                .collect(Collectors.joining(", "));
    }

    private String conversation(TicketAnswerGenerationContext context) {
        return context.ticket().messages()
                .stream()
                .map(message -> "%s: %s".formatted(message.senderType(), message.message()))
                .collect(Collectors.joining("\n"));
    }

    private GeneratedTicketAnswer toAnswer(LlmTicketAnswer response, List<ArticleId> usedArticleIds) {
        if (response == null || response.answerText() == null || response.confidenceScore() == null) {
            throw new AnswerGenerationException("AI returned incomplete ticket answer");
        }
        return new GeneratedTicketAnswer(response.answerText(), usedArticleIds, response.confidenceScore());
    }

    private List<ArticleId> usedArticleIds(ChatResponse response) {
        if (response == null) {
            return List.of();
        }
        var context = response.getMetadata().get(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT);
        if (!(context instanceof List<?> documents)) {
            return List.of();
        }
        return documents.stream()
                .filter(Document.class::isInstance)
                .map(Document.class::cast)
                .map(this::articleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private ArticleId articleId(Document document) {
        var value = document.getMetadata().get(FullArticleRagDocumentPostProcessor.ARTICLE_ID_METADATA_KEY);
        if (value == null) {
            return null;
        }
        return ArticleId.of(UUID.fromString(value.toString()));
    }

    private String value(String value) {
        return value == null || value.isBlank() ? "none" : value;
    }
}

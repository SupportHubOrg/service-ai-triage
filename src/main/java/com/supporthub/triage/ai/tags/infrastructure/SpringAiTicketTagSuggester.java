package com.supporthub.triage.ai.tags.infrastructure;

import com.supporthub.triage.ai.common.SafeAiLoggingAdvisor;
import com.supporthub.triage.ai.tags.application.CandidateTag;
import com.supporthub.triage.ai.tags.application.SuggestedTag;
import com.supporthub.triage.ai.tags.application.SuggestedTags;
import com.supporthub.triage.ai.tags.application.TagSuggestionContext;
import com.supporthub.triage.ai.tags.application.TagSuggestionException;
import com.supporthub.triage.ai.tags.application.TicketTagSuggester;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openaisdk.OpenAiSdkChatOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "application.ai.tag-suggestion", name = "provider", havingValue = "openai")
public class SpringAiTicketTagSuggester implements TicketTagSuggester {

    private static final String OPERATION = "ticket-tag-suggestion";

    private static final String SYSTEM_PROMPT = """
            You are a support ticket triage assistant.

            Your task is to choose which tags from a fixed catalog best describe a support ticket conversation.

            Rules:
            - Choose only tags from the provided catalog. Never invent new tags.
            - Copy each chosen tag name exactly as written in the catalog.
            - Choose a tag only when it is clearly justified by the conversation.
            - Use only facts present in the conversation; do not assume or guess.
            - If no tag clearly applies, return an empty list.
            """;

    private static final String USER_PROMPT_TEMPLATE = """
            Available tags (choose only from these):
            {catalog}

            Ticket conversation:
            {conversation}

            Select the tags from the catalog that best match this conversation.
            """;

    private final ChatClient chatClient;
    private final TagSuggestionProperties properties;

    public SpringAiTicketTagSuggester(@TagSuggestionChatClient ChatClient chatClient,
                                      TagSuggestionProperties properties) {
        this.chatClient = chatClient;
        this.properties = properties;
    }

    @Override
    public SuggestedTags suggest(TagSuggestionContext context) {
        if (context.conversation().isEmpty()) {
            throw new TagSuggestionException(
                    "No conversation available to suggest tags for ticket " + context.ticketId());
        }
        if (context.candidateTags().isEmpty()) {
            return new SuggestedTags(List.of());
        }

        var selection = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(user -> user.text(USER_PROMPT_TEMPLATE)
                        .param("catalog", catalog(context))
                        .param("conversation", conversation(context)))
                .options(chatOptions())
                .advisors(advisor -> advisor.params(advisorContext(context)))
                .call()
                .entity(LlmTagSelection.class);

        return resolve(selection, context);
    }

    private OpenAiSdkChatOptions chatOptions() {
        return OpenAiSdkChatOptions.builder()
                .model(properties.model())
                .maxCompletionTokens(properties.maxOutputTokens())
                .store(false)
                .build();
    }

    private Map<String, Object> advisorContext(TagSuggestionContext context) {
        return Map.of(
                SafeAiLoggingAdvisor.CONTEXT_OPERATION, OPERATION,
                SafeAiLoggingAdvisor.CONTEXT_TICKET_ID, context.ticketId(),
                SafeAiLoggingAdvisor.CONTEXT_ORGANIZATION_ID, context.organizationId(),
                SafeAiLoggingAdvisor.CONTEXT_MODEL, properties.model()
        );
    }

    private String catalog(TagSuggestionContext context) {
        return context.candidateTags().stream()
                .map(this::renderCandidate)
                .collect(Collectors.joining("\n"));
    }

    private String renderCandidate(CandidateTag tag) {
        if (tag.description() == null || tag.description().isBlank()) {
            return "- %s".formatted(tag.name());
        }
        return "- %s: %s".formatted(tag.name(), tag.description());
    }

    private String conversation(TagSuggestionContext context) {
        return context.conversation().stream()
                .map(message -> "%s: %s".formatted(message.senderType(), message.message()))
                .collect(Collectors.joining("\n"));
    }

    private SuggestedTags resolve(LlmTagSelection selection, TagSuggestionContext context) {
        if (selection == null || selection.tags() == null) {
            return new SuggestedTags(List.of());
        }
        var byName = new LinkedHashMap<String, CandidateTag>();
        for (var candidate : context.candidateTags()) {
            byName.put(normalize(candidate.name()), candidate);
        }
        var resolved = selection.tags().stream()
                .map(selected -> selected == null ? null : byName.get(normalize(selected.name())))
                .filter(Objects::nonNull)
                .distinct()
                .map(candidate -> new SuggestedTag(candidate.id(), candidate.name()))
                .toList();
        return new SuggestedTags(resolved);
    }

    private String normalize(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }
}

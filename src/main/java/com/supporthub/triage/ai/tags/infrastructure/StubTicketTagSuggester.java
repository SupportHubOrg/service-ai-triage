package com.supporthub.triage.ai.tags.infrastructure;

import com.supporthub.triage.ai.tags.application.TicketTagSuggester;

import com.supporthub.triage.ai.tags.application.TagSuggestionException;

import com.supporthub.triage.ai.tags.application.TagSuggestionContext;

import com.supporthub.triage.ai.tags.application.SuggestedTags;

import com.supporthub.triage.ai.tags.application.SuggestedTag;

import com.supporthub.triage.ai.tags.application.ConversationMessage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Placeholder suggester that deterministically selects a tag when its name occurs in the
 * conversation, so the end-to-end analysis flow works and is testable before the real model is
 * wired.
 *
 * <p>Keep this class as a local fallback/test double when no LLM-backed adapter is configured.
 */
@Component
@ConditionalOnProperty(prefix = "application.ai.tag-suggestion", name = "provider", havingValue = "stub",
        matchIfMissing = true)
public class StubTicketTagSuggester implements TicketTagSuggester {

    @Override
    public SuggestedTags suggest(TagSuggestionContext context) {
        if (context.conversation().isEmpty()) {
            throw new TagSuggestionException(
                    "No conversation available to suggest tags for ticket " + context.ticketId());
        }

        var transcript = context.conversation().stream()
                .map(ConversationMessage::message)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"))
                .toLowerCase(Locale.ROOT);

        var matched = context.candidateTags().stream()
                .filter(tag -> tag.name() != null && transcript.contains(tag.name().toLowerCase(Locale.ROOT)))
                .map(tag -> new SuggestedTag(tag.id(), tag.name()))
                .toList();

        return new SuggestedTags(matched);
    }
}

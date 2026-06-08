package com.supporthub.triage.ai;

/**
 * Port for choosing which of an organization's tags apply to a ticket conversation.
 *
 * <p>This is an external-integration boundary: implementations may perform network calls and must be
 * invoked outside the inbox/business transaction.
 */
public interface TicketTagSuggester {

    /**
     * @throws TagSuggestionException if no usable suggestion could be produced
     */
    SuggestedTags suggest(TagSuggestionContext context);
}

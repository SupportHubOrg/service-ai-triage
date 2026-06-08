package com.supporthub.triage.ai.tags.infrastructure;

import com.supporthub.triage.ai.tags.application.TagSuggestionException;

import com.supporthub.triage.ai.tags.application.TagSuggestionContext;

import com.supporthub.triage.ai.tags.application.ConversationMessage;

import com.supporthub.triage.ai.tags.application.CandidateTag;

import com.supporthub.triage.shared.identity.OrganizationId;
import com.supporthub.triage.shared.identity.TagId;
import com.supporthub.triage.shared.identity.TicketId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StubTicketTagSuggesterTest {

    private final StubTicketTagSuggester suggester = new StubTicketTagSuggester();

    @Test
    void shouldSelectCandidateTagsMentionedInConversation() {
        var billing = new CandidateTag(TagId.of(UUID.randomUUID()), "Billing", "Payments and invoices");
        var vpn = new CandidateTag(TagId.of(UUID.randomUUID()), "VPN", null);
        var context = new TagSuggestionContext(
                TicketId.of(UUID.randomUUID()),
                OrganizationId.of(UUID.randomUUID()),
                List.of(
                        new ConversationMessage("Employee", "I cannot connect to the VPN from home."),
                        new ConversationMessage("Operator", "Reset the VPN client profile.")
                ),
                List.of(billing, vpn)
        );

        var suggested = suggester.suggest(context).tags();

        assertThat(suggested)
                .singleElement()
                .satisfies(tag -> {
                    assertThat(tag.name()).isEqualTo("VPN");
                    assertThat(tag.id()).isEqualTo(vpn.id());
                });
    }

    @Test
    void shouldReturnEmptyWhenNoCandidateMatches() {
        var context = new TagSuggestionContext(
                TicketId.of(UUID.randomUUID()),
                OrganizationId.of(UUID.randomUUID()),
                List.of(new ConversationMessage("Employee", "My screen is flickering.")),
                List.of(new CandidateTag(TagId.of(UUID.randomUUID()), "Billing", null))
        );

        assertThat(suggester.suggest(context).tags()).isEmpty();
    }

    @Test
    void shouldFailWhenConversationIsEmpty() {
        var context = new TagSuggestionContext(
                TicketId.of(UUID.randomUUID()),
                OrganizationId.of(UUID.randomUUID()),
                List.of(),
                List.of(new CandidateTag(TagId.of(UUID.randomUUID()), "Billing", null))
        );

        assertThatThrownBy(() -> suggester.suggest(context))
                .isInstanceOf(TagSuggestionException.class);
    }
}

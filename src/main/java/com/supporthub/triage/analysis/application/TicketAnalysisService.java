package com.supporthub.triage.analysis.application;

import com.supporthub.triage.ai.answer.application.AnswerGenerationException;
import com.supporthub.triage.ai.answer.application.TicketAnswerGenerationContext;
import com.supporthub.triage.ai.answer.application.TicketAnswerGenerator;
import com.supporthub.triage.ai.tags.application.CandidateTag;
import com.supporthub.triage.ai.tags.application.ConversationMessage;
import com.supporthub.triage.ai.tags.application.SuggestedTag;
import com.supporthub.triage.ai.tags.application.SuggestedTags;
import com.supporthub.triage.ai.tags.application.TagSuggestionContext;
import com.supporthub.triage.ai.tags.application.TagSuggestionException;
import com.supporthub.triage.ai.tags.application.TicketTagSuggester;
import com.supporthub.triage.analysis.persistence.TicketAnalysisEntity;
import com.supporthub.triage.shared.identity.TagId;
import com.supporthub.triage.tag.application.AiEligibleTag;
import com.supporthub.triage.tag.application.OrganizationTagProjection;
import com.supporthub.triage.ticket.application.TicketAnalysisInput;
import com.supporthub.triage.ticket.application.TicketProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Orchestrates the durable two-stage AI triage pipeline.
 *
 * <p>Each stage is claimed and finalized through short transactions in {@link TicketAnalysisJobStore}.
 * External AI calls happen between those transactions: tag suggestion first, then answer generation
 * with the Spring AI RAG advisor chain. Persisting suggested tags between stages lets the service
 * resume from {@code PENDING_ANSWER_GENERATION} if it stops after tag analysis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TicketAnalysisService {

    private final TicketProjection ticketProjection;
    private final OrganizationTagProjection tagProjection;
    private final TicketTagSuggester tagSuggester;
    private final TicketAnswerGenerator answerGenerator;
    private final TicketAnalysisJobStore jobStore;

    public void processPendingAnalyses() {
        processTagAnalyses(jobStore.claimTagAnalysisBatch());
        processAnswerGenerations(jobStore.claimAnswerGenerationBatch());
    }

    public void analyzeTags(TicketAnalysisEntity analysis) {
        var input = findInputOrFail(analysis);
        if (input.isEmpty()) {
            return;
        }

        try {
            var suggestedTags = suggestTags(input.get());
            jobStore.saveSuggestedTagsAndQueueAnswer(analysis.getAnalysisId(), suggestedTags);
            log.info("Ticket analysis tag stage completed. analysisId={}, ticketId={}, selectedTags={}",
                    analysis.getAnalysisId(),
                    analysis.getTicketId(),
                    suggestedTags.tags().size());
        } catch (TagSuggestionException exception) {
            handleKnownAiFailure(analysis, exception);
        }
    }

    public void generateAnswer(TicketAnalysisEntity analysis) {
        var input = findInputOrFail(analysis);
        if (input.isEmpty()) {
            return;
        }

        try {
            var suggestedTags = jobStore.findSuggestedTags(analysis.getAnalysisId());
            var answer = answerGenerator.generate(new TicketAnswerGenerationContext(
                    input.get(),
                    suggestedTags,
                    strictRetrievalTags(input.get(), suggestedTags)
            ));

            jobStore.complete(new TicketAiAnalysisResult(
                    analysis.getAnalysisId(),
                    input.get().ticketId(),
                    input.get().organizationId(),
                    suggestedTagIds(suggestedTags),
                    answer.answerText(),
                    answer.usedKnowledgeArticleIds(),
                    answer.confidenceScore()
            ));
            log.info("Ticket analysis answer stage completed. analysisId={}, ticketId={}, usedArticles={}",
                    analysis.getAnalysisId(),
                    analysis.getTicketId(),
                    answer.usedKnowledgeArticleIds().size());
        } catch (AnswerGenerationException exception) {
            handleKnownAiFailure(analysis, exception);
        }
    }

    private void processTagAnalyses(List<TicketAnalysisEntity> analyses) {
        for (var analysis : analyses) {
            try {
                analyzeTags(analysis);
            } catch (RuntimeException exception) {
                handleUnexpectedFailure(analysis, exception);
            }
        }
    }

    private void processAnswerGenerations(List<TicketAnalysisEntity> analyses) {
        for (var analysis : analyses) {
            try {
                generateAnswer(analysis);
            } catch (RuntimeException exception) {
                handleUnexpectedFailure(analysis, exception);
            }
        }
    }

    private Optional<TicketAnalysisInput> findInputOrFail(TicketAnalysisEntity analysis) {
        var input = ticketProjection.findAnalysisInput(analysis.getTicketId());
        if (input.isPresent()) {
            return input;
        }
        jobStore.failTerminal(TicketAiAnalysisFailures.invalidTicketData(
                analysis,
                "Ticket projection is missing for analysis"
        ));
        return Optional.empty();
    }

    private SuggestedTags suggestTags(TicketAnalysisInput input) {
        var candidateTags = candidateTags(input);
        return tagSuggester.suggest(new TagSuggestionContext(
                input.ticketId(),
                input.organizationId(),
                conversation(input),
                candidateTags
        ));
    }

    private List<CandidateTag> candidateTags(TicketAnalysisInput input) {
        return tagProjection.findAiEligibleTags(input.organizationId())
                .stream()
                .map(this::toCandidateTag)
                .toList();
    }

    private CandidateTag toCandidateTag(AiEligibleTag tag) {
        return new CandidateTag(tag.tagId(), tag.name(), null);
    }

    private List<ConversationMessage> conversation(TicketAnalysisInput input) {
        var messages = new ArrayList<ConversationMessage>();
        if (input.subject() != null && !input.subject().isBlank()) {
            messages.add(new ConversationMessage("TicketSubject", input.subject()));
        }
        if (input.description() != null && !input.description().isBlank()) {
            messages.add(new ConversationMessage("TicketDescription", input.description()));
        }
        input.messages().stream()
                .map(message -> new ConversationMessage(message.senderType(), message.message()))
                .forEach(messages::add);
        return List.copyOf(messages);
    }

    private List<TagId> strictRetrievalTags(TicketAnalysisInput input, SuggestedTags suggestedTags) {
        if (!suggestedTags.tags().isEmpty()) {
            return suggestedTagIds(suggestedTags);
        }
        return input.tagIds();
    }

    private List<TagId> suggestedTagIds(SuggestedTags suggestedTags) {
        return suggestedTags.tags()
                .stream()
                .map(SuggestedTag::id)
                .toList();
    }

    private void handleKnownAiFailure(TicketAnalysisEntity analysis, RuntimeException exception) {
        handleFailure(analysis, exception, error -> TicketAiAnalysisFailures.invalidAiResponse(
                analysis,
                error.getMessage()
        ));
    }

    private void handleUnexpectedFailure(TicketAnalysisEntity analysis, RuntimeException exception) {
        handleFailure(analysis, exception, error -> TicketAiAnalysisFailures.technical(analysis, error));
    }

    private void handleFailure(TicketAnalysisEntity analysis,
                               RuntimeException exception,
                               Function<RuntimeException, TicketAiAnalysisFailure> terminalFailure) {
        if (jobStore.canRetry(analysis)) {
            jobStore.releaseForRetry(analysis.getAnalysisId(), exception.getMessage());
            log.warn("Ticket analysis stage released for retry. analysisId={}, ticketId={}, status={}, errorType={}",
                    analysis.getAnalysisId(),
                    analysis.getTicketId(),
                    analysis.getStatus(),
                    exception.getClass().getSimpleName());
            return;
        }
        log.warn("Ticket analysis failed after retry budget. analysisId={}, ticketId={}, status={}, errorType={}",
                analysis.getAnalysisId(),
                analysis.getTicketId(),
                analysis.getStatus(),
                exception.getClass().getSimpleName());
        jobStore.failTerminal(terminalFailure.apply(exception));
    }
}

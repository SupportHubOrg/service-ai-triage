package com.supporthub.triage.ai.answer.infrastructure;

import com.supporthub.triage.ai.answer.application.GeneratedTicketAnswer;
import com.supporthub.triage.ai.answer.application.TicketAnswerGenerationContext;
import com.supporthub.triage.ai.answer.application.TicketAnswerGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(prefix = "application.ai.answer-generation", name = "provider", havingValue = "stub",
        matchIfMissing = true)
class StubTicketAnswerGenerator implements TicketAnswerGenerator {

    @Override
    public GeneratedTicketAnswer generate(TicketAnswerGenerationContext context) {
        return new GeneratedTicketAnswer(
                "Stub answer generation completed for ticket '%s'.".formatted(context.ticket().subject()),
                List.of(),
                0.50
        );
    }
}

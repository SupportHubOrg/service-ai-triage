package com.supporthub.triage.ai.answer.application;

public interface TicketAnswerGenerator {

    GeneratedTicketAnswer generate(TicketAnswerGenerationContext context);
}

package com.supporthub.triage.analysis.application;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "application.analysis.enabled", havingValue = "true")
@RequiredArgsConstructor
public class TicketAnalysisScheduler {

    private final TicketAnalysisService analysisService;

    @Scheduled(fixedDelayString = "${application.analysis.poll-delay-ms}")
    @SchedulerLock(
            name = "ai-triage-ticket-analysis",
            lockAtMostFor = "${application.analysis.lock-at-most-for}",
            lockAtLeastFor = "${application.analysis.lock-at-least-for}"
    )
    public void analyzePendingTickets() {
        analysisService.processPendingAnalyses();
    }
}

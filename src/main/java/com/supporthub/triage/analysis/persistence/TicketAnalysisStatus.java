package com.supporthub.triage.analysis.persistence;

public enum TicketAnalysisStatus {
    PENDING_TAG_ANALYSIS,
    TAG_ANALYSIS_IN_PROGRESS,
    PENDING_ANSWER_GENERATION,
    ANSWER_GENERATION_IN_PROGRESS,
    COMPLETED,
    FAILED
}

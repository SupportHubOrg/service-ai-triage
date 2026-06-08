package com.supporthub.triage.messaging.outbox.core.model;

import com.supporthub.triage.messaging.event.AiAnalysisCompletedPayload;
import com.supporthub.triage.messaging.event.annotation.MessageKey;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessageKeyExtractorTest {

    private final MessageKeyExtractor extractor = new MessageKeyExtractor();

    @Test
    void shouldExtractAnnotatedComponentValueAsKey() {
        var ticketId = UUID.randomUUID();
        var payload = new AiAnalysisCompletedPayload(
                UUID.randomUUID(),
                ticketId,
                UUID.randomUUID(),
                List.of(UUID.randomUUID()),
                "Draft operator answer",
                List.of(UUID.randomUUID()),
                0.91
        );

        assertThat(extractor.extract(payload)).isEqualTo(ticketId.toString());
    }

    @Test
    void shouldRejectPayloadWithoutMessageKey() {
        assertThatThrownBy(() -> extractor.extract(new NoKeyPayload(UUID.randomUUID())))
                .isInstanceOf(MessageKeyResolutionException.class)
                .hasMessageContaining("Exactly one @MessageKey");
    }

    @Test
    void shouldRejectNullKeyValue() {
        assertThatThrownBy(() -> extractor.extract(new NullableKeyPayload(null)))
                .isInstanceOf(MessageKeyResolutionException.class)
                .hasMessageContaining("is null");
    }

    @Test
    void shouldRejectNonRecordPayload() {
        assertThatThrownBy(() -> extractor.extract(new NotARecordPayload()))
                .isInstanceOf(MessageKeyResolutionException.class)
                .hasMessageContaining("must be a record");
    }

    private record NoKeyPayload(UUID id) {
    }

    private record NullableKeyPayload(@MessageKey UUID key) {
    }

    private static final class NotARecordPayload {
    }
}

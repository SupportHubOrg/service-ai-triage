package com.supporthub.triage.messaging.consumer;

import com.supporthub.triage.messaging.inbox.core.context.InboxConsumers;
import com.supporthub.triage.messaging.inbox.core.processing.InboxMessageProcessor;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketManagementEventConsumer {

    private final InboxMessageProcessor inboxMessageProcessor;

    @KafkaListener(
            topics = "${application.kafka.consumer.ticket-management-events}",
            containerFactory = "inboxKafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, String> consumerRecord) {
        inboxMessageProcessor.process(InboxConsumers.TICKET_MANAGEMENT, consumerRecord);
    }
}

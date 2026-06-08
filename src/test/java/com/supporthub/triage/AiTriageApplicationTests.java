package com.supporthub.triage;

import com.supporthub.triage.messaging.event.TagAiSettingUpdatedPayload;
import com.supporthub.triage.messaging.event.TagCreatedPayload;
import com.supporthub.triage.messaging.event.TagDeletedPayload;
import com.supporthub.triage.messaging.inbox.core.context.InboxConsumers;
import com.supporthub.triage.messaging.inbox.core.processing.InboxMessageProcessor;
import com.supporthub.triage.tag.application.OrganizationTagProjection;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AiTriageApplicationTests {

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:16-alpine")
    )
            .withDatabaseName("triage")
            .withUsername("triage")
            .withPassword("triage");

    private static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("apache/kafka-native:3.8.0")
    );

    static {
        POSTGRES.start();
        KAFKA.start();
    }

    @DynamicPropertySource
    static void registerIntegrationProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @Autowired
    private OrganizationTagProjection tagProjection;

    @Autowired
    private InboxMessageProcessor inboxMessageProcessor;

    @Test
    void contextLoads() {
    }

    @Test
    void projectsOnlyAiEligibleTags() {
        var organizationId = UUID.randomUUID();
        var visibleTagId = UUID.randomUUID();
        var excludedTagId = UUID.randomUUID();
        var deletedTagId = UUID.randomUUID();

        tagProjection.project(new TagCreatedPayload(visibleTagId, organizationId, " Billing "));
        tagProjection.project(new TagCreatedPayload(excludedTagId, organizationId, "Internal"));
        tagProjection.project(new TagCreatedPayload(deletedTagId, organizationId, "Legacy"));
        tagProjection.project(new TagAiSettingUpdatedPayload(excludedTagId, organizationId, UUID.randomUUID(), true));
        tagProjection.project(new TagDeletedPayload(deletedTagId, organizationId, UUID.randomUUID()));

        var tags = tagProjection.findAiEligibleTags(organizationId);

        assertThat(tags)
                .singleElement()
                .satisfies(tag -> assertThat(tag.name()).isEqualTo("Billing"));
    }

    @Test
    void preservesAiSettingWhenUpdateArrivesBeforeCreate() {
        var organizationId = UUID.randomUUID();
        var tagId = UUID.randomUUID();

        tagProjection.project(new TagAiSettingUpdatedPayload(tagId, organizationId, UUID.randomUUID(), true));
        tagProjection.project(new TagCreatedPayload(tagId, organizationId, "Sensitive"));

        assertThat(tagProjection.findAiEligibleTags(organizationId)).isEmpty();
    }

    @Test
    void projectsTagCreatedFromTicketManagementEventEnvelope() {
        var organizationId = UUID.randomUUID();
        var tagId = UUID.randomUUID();
        var event = """
                {
                  "metadata": {
                    "eventId": "%s",
                    "eventType": "TagCreated",
                    "version": 1,
                    "timestamp": "2026-06-07T20:00:00Z",
                    "source": "ticket-management-service"
                  },
                  "payload": {
                    "tagId": "%s",
                    "organizationId": "%s",
                    "tagName": "Network"
                  }
                }
                """.formatted(UUID.randomUUID(), tagId, organizationId);

        inboxMessageProcessor.process(
                InboxConsumers.TICKET_MANAGEMENT,
                new ConsumerRecord<>("ticket-management-events", 0, 0L, null, event)
        );

        assertThat(tagProjection.findAiEligibleTags(organizationId))
                .singleElement()
                .satisfies(tag -> assertThat(tag.name()).isEqualTo("Network"));
    }
}

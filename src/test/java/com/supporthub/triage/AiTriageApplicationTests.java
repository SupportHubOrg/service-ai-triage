package com.supporthub.triage;

import com.supporthub.triage.knowledge.application.KnowledgeArticleProjection;
import com.supporthub.triage.knowledge.application.KnowledgeArticleEmbeddingIndexer;
import com.supporthub.triage.knowledge.persistence.KnowledgeArticleChunkIndexStatus;
import com.supporthub.triage.messaging.event.TagAiSettingUpdatedPayload;
import com.supporthub.triage.messaging.event.TagCreatedPayload;
import com.supporthub.triage.messaging.event.TagDeletedPayload;
import com.supporthub.triage.messaging.inbox.core.context.InboxConsumers;
import com.supporthub.triage.messaging.inbox.core.processing.InboxMessageProcessor;
import com.supporthub.triage.shared.identity.ArticleId;
import com.supporthub.triage.shared.identity.MessageId;
import com.supporthub.triage.shared.identity.OrganizationId;
import com.supporthub.triage.shared.identity.TagId;
import com.supporthub.triage.shared.identity.TicketId;
import com.supporthub.triage.shared.identity.UserId;
import com.supporthub.triage.tag.application.OrganizationTagProjection;
import com.supporthub.triage.ticket.application.TicketProjection;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
class AiTriageApplicationTests {

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("pgvector/pgvector:pg16").asCompatibleSubstituteFor("postgres")
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
    private KnowledgeArticleProjection articleProjection;

    @Autowired
    private KnowledgeArticleEmbeddingIndexer articleEmbeddingIndexer;

    @Autowired
    private TicketProjection ticketProjection;

    @Autowired
    private CapturingVectorStore capturingVectorStore;

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

        var tags = tagProjection.findAiEligibleTags(OrganizationId.of(organizationId));

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

        assertThat(tagProjection.findAiEligibleTags(OrganizationId.of(organizationId))).isEmpty();
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

        assertThat(tagProjection.findAiEligibleTags(OrganizationId.of(organizationId)))
                .singleElement()
                .satisfies(tag -> assertThat(tag.name()).isEqualTo("Network"));
    }

    @Test
    void projectsTicketAnalysisInputFromTicketManagementEvents() {
        var organizationId = UUID.randomUUID();
        var ticketId = UUID.randomUUID();
        var creatorId = UUID.randomUUID();
        var messageId = UUID.randomUUID();
        var senderId = UUID.randomUUID();
        var firstTagId = UUID.randomUUID();
        var secondTagId = UUID.randomUUID();

        inboxMessageProcessor.process(
                InboxConsumers.TICKET_MANAGEMENT,
                new ConsumerRecord<>("ticket-management-events", 0, 10L, null, """
                        {
                          "metadata": {
                            "eventId": "%s",
                            "eventType": "TicketCreated",
                            "version": 1,
                            "timestamp": "2026-06-08T11:00:00Z",
                            "source": "ticket-management-service"
                          },
                          "payload": {
                            "ticketId": "%s",
                            "organizationId": "%s",
                            "creatorId": "%s",
                            "subject": "Cannot receive email",
                            "description": "Customer reports missing inbound messages."
                          }
                        }
                        """.formatted(UUID.randomUUID(), ticketId, organizationId, creatorId))
        );
        inboxMessageProcessor.process(
                InboxConsumers.TICKET_MANAGEMENT,
                new ConsumerRecord<>("ticket-management-events", 0, 11L, null, """
                        {
                          "metadata": {
                            "eventId": "%s",
                            "eventType": "TicketMessageAdded",
                            "version": 1,
                            "timestamp": "2026-06-08T11:05:00Z",
                            "source": "ticket-management-service"
                          },
                          "payload": {
                            "ticketId": "%s",
                            "organizationId": "%s",
                            "messageId": "%s",
                            "senderId": "%s",
                            "senderType": "Employee",
                            "message": "Emails from the vendor are bouncing."
                          }
                        }
                        """.formatted(UUID.randomUUID(), ticketId, organizationId, messageId, senderId))
        );
        inboxMessageProcessor.process(
                InboxConsumers.TICKET_MANAGEMENT,
                new ConsumerRecord<>("ticket-management-events", 0, 12L, null, """
                        {
                          "metadata": {
                            "eventId": "%s",
                            "eventType": "TicketTagUpdated",
                            "version": 1,
                            "timestamp": "2026-06-08T11:06:00Z",
                            "source": "ticket-management-service"
                          },
                          "payload": {
                            "ticketId": "%s",
                            "organizationId": "%s",
                            "updatedById": "%s",
                            "currentTags": ["%s", "%s"]
                          }
                        }
                        """.formatted(UUID.randomUUID(), ticketId, organizationId, UUID.randomUUID(), firstTagId, secondTagId))
        );

        assertThat(ticketProjection.findAnalysisInput(TicketId.of(ticketId)))
                .hasValueSatisfying(input -> {
                    assertThat(input.organizationId()).isEqualTo(OrganizationId.of(organizationId));
                    assertThat(input.creatorId()).isEqualTo(UserId.of(creatorId));
                    assertThat(input.subject()).isEqualTo("Cannot receive email");
                    assertThat(input.description()).contains("missing inbound messages");
                    assertThat(input.messages())
                            .singleElement()
                            .satisfies(message -> {
                                assertThat(message.messageId()).isEqualTo(MessageId.of(messageId));
                                assertThat(message.senderType()).isEqualTo("Employee");
                                assertThat(message.message()).contains("vendor are bouncing");
                            });
                    assertThat(input.tagIds()).containsExactlyInAnyOrder(TagId.of(firstTagId), TagId.of(secondTagId));
                });
    }

    @Test
    void projectsKnowledgeArticleApprovedFromKnowledgeManagementEventEnvelope() {
        var organizationId = UUID.randomUUID();
        var articleId = UUID.randomUUID();
        var draftId = UUID.randomUUID();
        var sourceTicketId = UUID.randomUUID();
        var publishedById = UUID.randomUUID();
        var firstTagId = UUID.randomUUID();
        var secondTagId = UUID.randomUUID();
        var event = """
                {
                  "metadata": {
                    "eventId": "%s",
                    "eventType": "KnowledgeArticleApproved",
                    "version": 1,
                    "timestamp": "2026-06-08T10:15:00Z",
                    "source": "knowledge-management-service"
                  },
                  "payload": {
                    "articleId": "%s",
                    "draftId": "%s",
                    "sourceTicketId": "%s",
                    "organizationId": "%s",
                    "publishedById": "%s",
                    "title": "Reset password article",
                    "articleContent": "Step 1. Open account settings. Step 2. Request a reset link. Step 3. Confirm the new password.",
                    "tags": ["%s", "%s"]
                  }
                }
                """.formatted(
                UUID.randomUUID(),
                articleId,
                draftId,
                sourceTicketId,
                organizationId,
                publishedById,
                firstTagId,
                secondTagId
        );

        inboxMessageProcessor.process(
                InboxConsumers.KNOWLEDGE_MANAGEMENT,
                new ConsumerRecord<>("knowledge-base-events", 0, 0L, null, event)
        );

        assertThat(articleProjection.findChunks(ArticleId.of(articleId)))
                .singleElement()
                .satisfies(chunk -> {
                    assertThat(chunk.getOrganizationId()).isEqualTo(OrganizationId.of(organizationId));
                    assertThat(chunk.getChunkIndex()).isZero();
                    assertThat(chunk.getChunkCount()).isEqualTo(1);
                    assertThat(chunk.getEmbeddingModel()).isEqualTo("text-embedding-3-small");
                    assertThat(chunk.getEmbeddingDimensions()).isEqualTo(1536);
                    assertThat(chunk.getIndexStatus()).isEqualTo(KnowledgeArticleChunkIndexStatus.PENDING);
                    assertThat(chunk.getContent()).contains("Request a reset link");
                });
        assertThat(articleProjection.findTagIds(ArticleId.of(articleId)))
                .containsExactlyInAnyOrder(TagId.of(firstTagId), TagId.of(secondTagId));
    }

    @Test
    void indexesPendingKnowledgeArticleChunks() {
        var organizationId = UUID.randomUUID();
        var articleId = UUID.randomUUID();
        var tagId = UUID.randomUUID();
        var event = """
                {
                  "metadata": {
                    "eventId": "%s",
                    "eventType": "KnowledgeArticleApproved",
                    "version": 1,
                    "timestamp": "2026-06-08T10:18:00Z",
                    "source": "knowledge-management-service"
                  },
                  "payload": {
                    "articleId": "%s",
                    "draftId": "%s",
                    "sourceTicketId": "%s",
                    "organizationId": "%s",
                    "publishedById": "%s",
                    "title": "Email delivery troubleshooting",
                    "articleContent": "Mail delivery failed because SPF was missing. Add the sender include to SPF and retry delivery.",
                    "tags": ["%s"]
                  }
                }
                """.formatted(
                UUID.randomUUID(),
                articleId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                organizationId,
                UUID.randomUUID(),
                tagId
        );

        inboxMessageProcessor.process(
                InboxConsumers.KNOWLEDGE_MANAGEMENT,
                new ConsumerRecord<>("knowledge-base-events", 0, 3L, null, event)
        );

        var indexed = articleEmbeddingIndexer.indexPendingChunks();

        assertThat(indexed).isPositive();
        assertThat(articleProjection.findChunks(ArticleId.of(articleId)))
                .singleElement()
                .satisfies(chunk -> {
                    assertThat(chunk.getIndexStatus()).isEqualTo(KnowledgeArticleChunkIndexStatus.INDEXED);
                    assertThat(chunk.getIndexedAt()).isNotNull();
                });
        assertThat(capturingVectorStore.documents())
                .filteredOn(document -> articleId.toString().equals(document.getMetadata().get("articleId")))
                .singleElement()
                .satisfies(document -> {
                    assertThat(document.getText()).contains("SPF was missing");
                    assertThat(document.getMetadata()).containsEntry("organizationId", organizationId.toString());
                    assertThat(document.getMetadata()).containsEntry("articleId", articleId.toString());
                    assertThat(document.getMetadata()).containsEntry("tagIds", tagId.toString());
                    assertThat(document.getMetadata()).containsEntry("docType", "knowledge_article");
                    assertThat(document.getMetadata()).containsEntry("status", "approved");
                });
    }

    @Test
    void keepsKnowledgeArticleProjectionIdempotentForDuplicateApprovedEvent() {
        var organizationId = UUID.randomUUID();
        var articleId = UUID.randomUUID();
        var event = """
                {
                  "metadata": {
                    "eventId": "%s",
                    "eventType": "KnowledgeArticleApproved",
                    "version": 1,
                    "timestamp": "2026-06-08T10:20:00Z",
                    "source": "knowledge-management-service"
                  },
                  "payload": {
                    "articleId": "%s",
                    "draftId": "%s",
                    "sourceTicketId": "%s",
                    "organizationId": "%s",
                    "publishedById": "%s",
                    "title": "VPN troubleshooting",
                    "articleContent": "The user cannot connect to VPN. Ask the user to refresh MFA, check the client profile, and retry connection.",
                    "tags": []
                  }
                }
                """.formatted(
                UUID.randomUUID(),
                articleId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                organizationId,
                UUID.randomUUID()
        );
        var record = new ConsumerRecord<String, String>("knowledge-base-events", 0, 1L, null, event);

        inboxMessageProcessor.process(InboxConsumers.KNOWLEDGE_MANAGEMENT, record);
        inboxMessageProcessor.process(InboxConsumers.KNOWLEDGE_MANAGEMENT, record);

        assertThat(articleProjection.findChunks(ArticleId.of(articleId))).hasSize(1);
    }

    @Test
    void ignoresKnowledgeManagementEventsThatAreNotUsedByTriage() {
        var event = """
                {
                  "metadata": {
                    "eventId": "%s",
                    "eventType": "KnowledgeArticleRejected",
                    "version": 1,
                    "timestamp": "2026-06-08T10:25:00Z",
                    "source": "knowledge-management-service"
                  },
                  "payload": {
                    "draftId": "%s",
                    "sourceTicketId": "%s",
                    "organizationId": "%s",
                    "rejectedById": "%s",
                    "reason": "Insufficient information"
                  }
                }
                """.formatted(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        assertThatCode(() -> inboxMessageProcessor.process(
                InboxConsumers.KNOWLEDGE_MANAGEMENT,
                new ConsumerRecord<>("knowledge-base-events", 0, 2L, null, event)
        )).doesNotThrowAnyException();
    }

    @TestConfiguration
    static class VectorStoreTestConfig {

        @Bean
        CapturingVectorStore capturingVectorStore() {
            return new CapturingVectorStore();
        }
    }

    static class CapturingVectorStore implements VectorStore {

        private final List<Document> documents = new ArrayList<>();

        @Override
        public void add(List<Document> documents) {
            this.documents.addAll(documents);
        }

        @Override
        public void delete(List<String> idList) {
            documents.removeIf(document -> idList.contains(document.getId()));
        }

        @Override
        public void delete(Filter.Expression filterExpression) {
            documents.clear();
        }

        @Override
        public List<Document> similaritySearch(SearchRequest request) {
            return List.copyOf(documents);
        }

        List<Document> documents() {
            return List.copyOf(documents);
        }
    }
}

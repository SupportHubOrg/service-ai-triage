package com.supporthub.triage.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.openaisdk.OpenAiSdkChatModel;
import org.springframework.ai.openaisdk.OpenAiSdkChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.List;

@Configuration
@ConditionalOnProperty(prefix = "application.ai.tag-suggestion", name = "provider", havingValue = "openai")
class TagSuggestionConfig {

    @Bean
    OpenAiSdkChatModel tagSuggestionChatModel(TagSuggestionProperties properties,
                                              @Value("${spring.ai.openai-sdk.api-key:}") String apiKey) {
        if (!StringUtils.hasText(apiKey)) {
            throw new TagSuggestionException("OPENAI_API_KEY is required when tag suggestion provider is openai");
        }
        return OpenAiSdkChatModel.builder()
                .options(OpenAiSdkChatOptions.builder()
                        .apiKey(apiKey)
                        .model(properties.model())
                        .store(false)
                        .build())
                .build();
    }

    @Bean
    @TagSuggestionChatClient
    ChatClient tagSuggestionChatClient(OpenAiSdkChatModel tagSuggestionChatModel,
                                       SecretSanitizer secretSanitizer,
                                       TagSuggestionProperties properties) {
        return ChatClient.builder(tagSuggestionChatModel)
                .defaultAdvisors(advisors(secretSanitizer, properties))
                .build();
    }

    private List<Advisor> advisors(SecretSanitizer secretSanitizer, TagSuggestionProperties properties) {
        return List.of(
                new CredentialScrubbingAdvisor(secretSanitizer),
                new SafeAiLoggingAdvisor(properties)
        );
    }
}

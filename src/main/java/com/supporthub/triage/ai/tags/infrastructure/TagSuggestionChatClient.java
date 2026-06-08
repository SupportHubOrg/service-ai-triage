package com.supporthub.triage.ai.tags.infrastructure;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the {@link org.springframework.ai.chat.client.ChatClient} dedicated to ticket tag
 * suggestion. As more AI use cases are added each gets its own qualifier annotation, so the right
 * {@code ChatClient} bean is selected explicitly at the injection point instead of by bean name.
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Qualifier
public @interface TagSuggestionChatClient {
}

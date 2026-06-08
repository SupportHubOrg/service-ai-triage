package com.supporthub.triage.ai.tags.application;

/**
 * One message of a ticket conversation, as input to the tag suggester. Deliberately slim: no ids are
 * passed to the model, only the author role and the message body.
 *
 * @param senderType role of the author (e.g. {@code Employee}, {@code Operator}, {@code AI})
 * @param message    message body
 */
public record ConversationMessage(
        String senderType,
        String message
) {
}

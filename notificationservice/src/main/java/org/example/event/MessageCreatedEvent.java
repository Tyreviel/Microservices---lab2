package org.example.event;

public record MessageCreatedEvent(
    String messageId,
    String sender,
    String recipient,
    String content,
    long timestamp
) {}

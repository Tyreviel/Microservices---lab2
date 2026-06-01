package org.example.chatservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.chatservice.model.ChatMessageEntity;
import org.example.chatservice.model.OutboxEvent;
import org.example.chatservice.repository.ChatMessageRepository;
import org.example.chatservice.repository.OutboxRepository;
import org.example.event.MessageCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ChatService {
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatMessageRepository chatMessageRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public ChatService(ChatMessageRepository chatMessageRepository, 
                       OutboxRepository outboxRepository, 
                       ObjectMapper objectMapper) {
        this.chatMessageRepository = chatMessageRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public String saveMessageAndOutbox(String sender, String recipient, String content) {
        log.info("Saving message from {} to {}", sender, recipient);
        
        // 1. Save Chat Message
        ChatMessageEntity message = new ChatMessageEntity(sender, recipient, content);
        ChatMessageEntity savedMessage = chatMessageRepository.save(message);
        
        String messageId = savedMessage.getId().toString();
        
        // 2. Create Event Payload
        MessageCreatedEvent event = new MessageCreatedEvent(
            messageId,
            sender,
            recipient,
            content,
            System.currentTimeMillis()
        );
        
        try {
            String payload = objectMapper.writeValueAsString(event);
            
            // 3. Save Outbox Event
            OutboxEvent outboxEvent = new OutboxEvent(
                UUID.randomUUID(),
                "CHAT_MESSAGE",
                messageId,
                "MESSAGE_CREATED",
                payload
            );
            
            outboxRepository.save(outboxEvent);
            log.info("Message saved with ID {} and outbox event created", messageId);
            
            return messageId;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message event", e);
            throw new RuntimeException("Error processing message", e);
        }
    }
}

package org.example.chatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.chatservice.model.ChatMessageEntity;
import org.example.chatservice.model.OutboxEvent;
import org.example.chatservice.repository.ChatMessageRepository;
import org.example.chatservice.repository.OutboxRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ChatService chatService;

    @Test
    void saveMessageAndOutbox_ShouldSaveBoth() throws Exception {
        // Arrange
        String sender = "user1";
        String recipient = "user2";
        String content = "Hello";
        
        ChatMessageEntity mockMessage = new ChatMessageEntity(sender, recipient, content);
        // Manually set ID since it's a mock
        java.lang.reflect.Field idField = ChatMessageEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(mockMessage, 1L);

        when(chatMessageRepository.save(any())).thenReturn(mockMessage);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // Act
        String messageId = chatService.saveMessageAndOutbox(sender, recipient, content);

        // Assert
        assertEquals("1", messageId);
        verify(chatMessageRepository, times(1)).save(any(ChatMessageEntity.class));
        verify(outboxRepository, times(1)).save(any(OutboxEvent.class));
    }
}

package org.example.chatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.chatservice.config.RabbitConfig;
import org.example.chatservice.model.OutboxEvent;
import org.example.chatservice.repository.OutboxRepository;
import org.example.event.MessageCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxRelayTest {

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxRelay outboxRelay;

    @Test
    void relayEvents_ShouldSendPendingEventsToRabbit() throws Exception {
        // Arrange
        OutboxEvent event = new OutboxEvent(UUID.randomUUID(), "TYPE", "1", "MESSAGE_CREATED", "{}");
        // Set ID via reflection
        java.lang.reflect.Field idField = OutboxEvent.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(event, 1L);

        when(outboxRepository.findByStatus(OutboxEvent.OutboxStatus.PENDING)).thenReturn(List.of(event));
        when(objectMapper.readValue(anyString(), eq(MessageCreatedEvent.class)))
            .thenReturn(new MessageCreatedEvent("1", "s", "r", "c", 0L));

        // Act
        outboxRelay.relayEvents();

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(
            eq(RabbitConfig.EXCHANGE_NAME),
            eq("chat.message.created"),
            any(MessageCreatedEvent.class),
            any(),
            any()
        );
    }
}

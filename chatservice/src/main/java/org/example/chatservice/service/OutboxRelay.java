package org.example.chatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.chatservice.config.RabbitConfig;
import org.example.chatservice.model.OutboxEvent;
import org.example.chatservice.repository.OutboxRepository;
import org.example.event.MessageCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OutboxRelay {
    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public OutboxRelay(OutboxRepository outboxRepository, RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;

        this.rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack && correlationData != null) {
                Long id = Long.valueOf(correlationData.getId());
                updateStatus(id, OutboxEvent.OutboxStatus.PROCESSED);
                log.info("Message {} successfully published and acked", id);
            } else if (correlationData != null) {
                Long id = Long.valueOf(correlationData.getId());
                log.error("Message {} failed to publish: {}", id, cause);
            }
        });
    }

    private void updateStatus(Long id, OutboxEvent.OutboxStatus status) {
        outboxRepository.findById(id).ifPresent(event -> {
            event.setStatus(status);
            outboxRepository.save(event);
        });
    }

    @Scheduled(fixedDelay = 5000)
    public void relayEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository.findByStatus(OutboxEvent.OutboxStatus.PENDING);
        for (OutboxEvent event : pendingEvents) {
            try {
                log.info("Relaying pending event {} (Type: {})", event.getEventId(), event.getType());
                
                MessageCreatedEvent messagePayload = objectMapper.readValue(event.getPayload(), MessageCreatedEvent.class);
                CorrelationData correlationData = new CorrelationData(event.getId().toString());

                rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE_NAME,
                    "chat.message.created",
                    messagePayload,
                    message -> {
                        message.getMessageProperties().setHeader("X-Sender-App", "ChatService");
                        return message;
                    },
                    correlationData
                );
            } catch (Exception e) {
                log.error("Error relaying event {}: {}", event.getId(), e.getMessage());
            }
        }
    }
}

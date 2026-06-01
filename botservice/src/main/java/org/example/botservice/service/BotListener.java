package org.example.botservice.service;

import org.example.event.MessageCreatedEvent;
import org.example.grpc.chat.*;
import org.example.botservice.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class BotListener {
    private static final Logger log = LoggerFactory.getLogger(BotListener.class);
    private static final String BOT_NAME = "SupportBot";

    private final ChatServiceGrpc.ChatServiceBlockingStub chatStub;

    public BotListener(ChatServiceGrpc.ChatServiceBlockingStub chatStub) {
        this.chatStub = chatStub;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void handleMessageCreated(MessageCreatedEvent event) {
        log.info("Bot received event: {} from {}", event.content(), event.sender());

        // Prevent infinite loops by not responding to ourselves
        if (BOT_NAME.equals(event.sender())) {
            return;
        }

        // Logic for automatic response
        String responseContent = generateResponse(event.content(), event.sender());
        
        if (responseContent != null) {
            log.info("Bot responding with: {}", responseContent);
            
            ChatMessage responseMessage = ChatMessage.newBuilder()
                    .setSender(BOT_NAME)
                    .setRecipient(event.sender())
                    .setContent(responseContent)
                    .build();
            
            try {
                chatStub.sendMessage(responseMessage);
                log.info("Bot response successfully sent through ChatService");
            } catch (Exception e) {
                log.error("Failed to send bot response: {}", e.getMessage());
            }
        }
    }

    private String generateResponse(String content, String sender) {
        String lowerContent = content.toLowerCase();
        if (lowerContent.contains("hello") || lowerContent.contains("hi")) {
            return "Hello " + sender + "! How can I help you today?";
        } else if (lowerContent.contains("help")) {
            return "I am an automated support bot. You can ask me about our services!";
        } else if (lowerContent.contains("status")) {
            return "All systems are operational. Beep boop!";
        }
        return "I'm not sure how to respond to that, but I've noted your message: '" + content + "'";
    }
}

package org.example.notificationservice.service;

import org.example.notificationservice.config.RabbitConfig;
import org.example.event.MessageCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {
    private static final Logger logger = LoggerFactory.getLogger(NotificationListener.class);

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void handleMessageCreated(MessageCreatedEvent event) {
        logger.info("New message notification for {}: '{}' from {}", 
            event.recipient(), event.content(), event.sender());
        
        // In a real app, this would send a push notification, email, etc.
        simulateSendingNotification(event);
    }

    private void simulateSendingNotification(MessageCreatedEvent event) {
        logger.info("Notification successfully sent to user {}", event.recipient());
    }
}

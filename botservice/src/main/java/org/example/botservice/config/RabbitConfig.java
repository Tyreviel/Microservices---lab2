package org.example.botservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String QUEUE_NAME = "bot.queue";
    public static final String EXCHANGE_NAME = "chat.exchange";
    public static final String ROUTING_KEY = "chat.message.created";

    @Bean
    public Queue botQueue() {
        return new Queue(QUEUE_NAME);
    }

    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue botQueue, TopicExchange chatExchange) {
        return BindingBuilder.bind(botQueue).to(chatExchange).with(ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

package org.example.botservice.config;

import org.example.grpc.chat.ChatServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcClientConfig {

    @Bean
    public ChatServiceGrpc.ChatServiceBlockingStub chatServiceBlockingStub(GrpcChannelFactory channelFactory) {
        return ChatServiceGrpc.newBlockingStub(channelFactory.createChannel("default-channel"));
    }
}

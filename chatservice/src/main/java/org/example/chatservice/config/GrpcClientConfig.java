package org.example.chatservice.config;

import org.example.grpc.user.UserServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcClientConfig {

    @Bean
    public UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub(GrpcChannelFactory channelFactory) {
        return UserServiceGrpc.newBlockingStub(channelFactory.createChannel("default-channel"));
    }
}

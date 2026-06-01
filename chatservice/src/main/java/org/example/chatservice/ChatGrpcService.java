package org.example.chatservice;

import io.grpc.stub.StreamObserver;
import org.example.grpc.chat.*;
import org.springframework.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@GrpcService
public class ChatGrpcService extends ChatServiceGrpc.ChatServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(ChatGrpcService.class);
    
    // In-memory store for demo purposes
    private final List<ChatMessage> messageHistory = new ArrayList<>();

    @Override
    public void sendMessage(ChatMessage request, StreamObserver<ChatResponse> responseObserver) {
        log.info("Received message from {} to {}: {}", request.getSender(), request.getRecipient(), request.getContent());
        
        ChatMessage messageWithTimestamp = ChatMessage.newBuilder(request)
                .setTimestamp(System.currentTimeMillis())
                .build();
        
        synchronized (messageHistory) {
            messageHistory.add(messageWithTimestamp);
        }

        ChatResponse response = ChatResponse.newBuilder()
                .setSuccess(true)
                .setMessageId(UUID.randomUUID().toString())
                .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getChatHistory(HistoryRequest request, StreamObserver<HistoryResponse> responseObserver) {
        log.info("Fetching history for user {}", request.getUserId());
        
        List<ChatMessage> filtered;
        synchronized (messageHistory) {
            filtered = messageHistory.stream()
                    .filter(m -> (m.getSender().equals(request.getUserId()) && m.getRecipient().equals(request.getContactId())) ||
                                 (m.getSender().equals(request.getContactId()) && m.getRecipient().equals(request.getUserId())))
                    .limit(request.getLimit() > 0 ? request.getLimit() : 50)
                    .toList();
        }

        HistoryResponse response = HistoryResponse.newBuilder()
                .addAllMessages(filtered)
                .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

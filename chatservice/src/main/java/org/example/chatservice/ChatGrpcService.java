package org.example.chatservice;

import org.example.chatservice.model.ChatMessageEntity;
import org.example.chatservice.repository.ChatMessageRepository;
import org.example.chatservice.service.ChatService;
import io.grpc.stub.StreamObserver;
import org.example.grpc.chat.*;
import org.springframework.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@GrpcService
public class ChatGrpcService extends ChatServiceGrpc.ChatServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(ChatGrpcService.class);
    
    private final ChatService chatService;
    private final ChatMessageRepository chatMessageRepository;

    public ChatGrpcService(ChatService chatService, ChatMessageRepository chatMessageRepository) {
        this.chatService = chatService;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public void sendMessage(ChatMessage request, StreamObserver<ChatResponse> responseObserver) {
        log.info("Received message from {} to {}: {}", request.getSender(), request.getRecipient(), request.getContent());
        
        String messageId = chatService.saveMessageAndOutbox(
            request.getSender(), 
            request.getRecipient(), 
            request.getContent()
        );

        ChatResponse response = ChatResponse.newBuilder()
                .setSuccess(true)
                .setMessageId(messageId)
                .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getChatHistory(HistoryRequest request, StreamObserver<HistoryResponse> responseObserver) {
        log.info("Fetching history for user {}", request.getUserId());
        
        List<ChatMessageEntity> messages = chatMessageRepository.findHistory(request.getUserId(), request.getContactId());
        
        List<ChatMessage> grpcMessages = messages.stream()
                .map(m -> ChatMessage.newBuilder()
                        .setSender(m.getSender())
                        .setRecipient(m.getRecipient())
                        .setContent(m.getContent())
                        .setTimestamp(m.getTimestamp().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .build())
                .limit(request.getLimit() > 0 ? request.getLimit() : 50)
                .toList();

        HistoryResponse response = HistoryResponse.newBuilder()
                .addAllMessages(grpcMessages)
                .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

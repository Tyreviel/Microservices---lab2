package org.example.chatservice;

import io.grpc.stub.StreamObserver;
import org.example.chatservice.model.ChatMessageEntity;
import org.example.chatservice.repository.ChatMessageRepository;
import org.example.chatservice.service.ChatService;
import org.example.grpc.chat.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatGrpcServiceTest {

    @Mock
    private ChatService chatService;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private StreamObserver<ChatResponse> responseObserver;

    @Mock
    private StreamObserver<HistoryResponse> historyObserver;

    @InjectMocks
    private ChatGrpcService chatGrpcService;

    @Test
    void sendMessage_ShouldCallChatService() {
        // Arrange
        ChatMessage request = ChatMessage.newBuilder()
                .setSender("user1")
                .setRecipient("user2")
                .setContent("Hi")
                .build();
        when(chatService.saveMessageAndOutbox(anyString(), anyString(), anyString())).thenReturn("123");

        // Act
        chatGrpcService.sendMessage(request, responseObserver);

        // Assert
        verify(chatService).saveMessageAndOutbox("user1", "user2", "Hi");
        verify(responseObserver).onNext(any(ChatResponse.class));
        verify(responseObserver).onCompleted();
    }

    @Test
    void getChatHistory_ShouldReturnHistory() {
        // Arrange
        HistoryRequest request = HistoryRequest.newBuilder()
                .setUserId("user1")
                .setContactId("user2")
                .build();
        ChatMessageEntity msg = new ChatMessageEntity("user1", "user2", "Hi");
        // Reflection for timestamp since it's set in constructor but I want to be sure
        when(chatMessageRepository.findHistory(anyString(), anyString())).thenReturn(List.of(msg));

        // Act
        chatGrpcService.getChatHistory(request, historyObserver);

        // Assert
        verify(chatMessageRepository).findHistory("user1", "user2");
        verify(historyObserver).onNext(any(HistoryResponse.class));
        verify(historyObserver).onCompleted();
    }
}

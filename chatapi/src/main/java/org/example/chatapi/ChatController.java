package org.example.chatapi;

import org.example.grpc.chat.*;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    private final ChatServiceGrpc.ChatServiceBlockingStub chatStub;

    public ChatController(ChatServiceGrpc.ChatServiceBlockingStub chatStub) {
        this.chatStub = chatStub;
    }

    @PostMapping("/send")
    public ChatResponse sendMessage(@RequestBody MessageRequest request) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("User {} sending message to {}", currentUser, request.recipient());
        
        ChatMessage chatMessage = ChatMessage.newBuilder()
                .setSender(currentUser)
                .setRecipient(request.recipient())
                .setContent(request.content())
                .build();
        
        var grpcResponse = chatStub.sendMessage(chatMessage);
        return new ChatResponse(grpcResponse.getSuccess(), grpcResponse.getMessageId());
    }

    @GetMapping("/history")
    public HistoryResponse getHistory(@RequestParam String contactId, @RequestParam(defaultValue = "50") int limit) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("User {} fetching history with {}", currentUser, contactId);
        
        HistoryRequest request = HistoryRequest.newBuilder()
                .setUserId(currentUser)
                .setContactId(contactId)
                .setLimit(limit)
                .build();
        
        var grpcResponse = chatStub.getChatHistory(request);
        
        var messages = grpcResponse.getMessagesList().stream()
                .map(m -> new ChatMessageDto(m.getSender(), m.getRecipient(), m.getContent(), m.getTimestamp()))
                .toList();
        
        return new HistoryResponse(messages);
    }
}

record MessageRequest(String recipient, String content) {}
record ChatMessageDto(String sender, String recipient, String content, long timestamp) {}
record HistoryResponse(java.util.List<ChatMessageDto> messages) {}
record ChatResponse(boolean success, String messageId) {}

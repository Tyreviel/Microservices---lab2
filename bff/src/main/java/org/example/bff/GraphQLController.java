package org.example.bff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestClient;

import java.util.concurrent.CompletableFuture;

@Controller
public class GraphQLController {

    private static final Logger log = LoggerFactory.getLogger(GraphQLController.class);

    private final Oauth2JwtTokenService tokenService;
    private final RestClient client1 = RestClient.create("http://localhost:8081");
    private final RestClient client2 = RestClient.create("http://localhost:8082");
    private final RestClient client3 = RestClient.create("http://localhost:8083");

    public GraphQLController(Oauth2JwtTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @QueryMapping
    public CompletableFuture<UserProfile> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String jwtToken = tokenService.getAccessToken(auth);

        return CompletableFuture.supplyAsync(() -> {
            log.info("GraphQL: Fetching profile for current user");
            return client2.get()
                    .uri("/api/users/me")
                    .headers(h -> h.setBearerAuth(jwtToken))
                    .retrieve()
                    .body(UserProfile.class);
        });
    }

    @QueryMapping
    public CompletableFuture<UserProfile> userProfile(@Argument String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String jwtToken = tokenService.getAccessToken(auth);

        return CompletableFuture.supplyAsync(() -> {
            log.info("GraphQL: Fetching profile for user {}", username);
            return client2.get()
                    .uri("/api/users/{username}", username)
                    .headers(h -> h.setBearerAuth(jwtToken))
                    .retrieve()
                    .body(UserProfile.class);
        });
    }

    @QueryMapping
    public Result merged() {
        log.info("---> Anropade huvud-query: merged");
        return new Result(null, null, null);
    }

    @QueryMapping
    public CompletableFuture<HistoryResponse> chatHistory(@Argument String contactId, @Argument Integer limit) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String jwtToken = tokenService.getAccessToken(auth);
        int finalLimit = limit != null ? limit : 50;

        return CompletableFuture.supplyAsync(() -> {
            log.info("GraphQL: Fetching chat history with {}", contactId);
            return client1.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/chat/history")
                            .queryParam("contactId", contactId)
                            .queryParam("limit", finalLimit)
                            .build())
                    .headers(h -> h.setBearerAuth(jwtToken))
                    .retrieve()
                    .body(HistoryResponse.class);
        });
    }

    @MutationMapping
    public CompletableFuture<ChatResponse> sendMessage(@Argument String recipient, @Argument String content) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String jwtToken = tokenService.getAccessToken(auth);

        return CompletableFuture.supplyAsync(() -> {
            log.info("GraphQL: Sending message to {}", recipient);
            return client1.post()
                    .uri("/api/chat/send")
                    .headers(h -> h.setBearerAuth(jwtToken))
                    .body(new MessageRequest(recipient, content))
                    .retrieve()
                    .body(ChatResponse.class);
        });
    }

    @SchemaMapping(typeName = "Result", field = "service1")
    public CompletableFuture<String> getService1(Result result) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // 2. Skicka med den till servicen
        String jwtToken = tokenService.getAccessToken(auth);

        return CompletableFuture.supplyAsync(() -> {
            log.info("Exekverar: Hämtar data från Service 1");

            try {
                return client1.get()
                        .uri("/api/test")
                        .headers(h -> h.setBearerAuth(jwtToken))
                        .retrieve()
                        .body(String.class);
            } catch (Exception e) {
                return "Service 1 (ChatAPI) unreachable or endpoint changed";
            }
        });
    }

    @SchemaMapping(typeName = "Result", field = "service2")
    public CompletableFuture<String> getService2(Result result) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String jwtToken = tokenService.getAccessToken(auth);
        return CompletableFuture.supplyAsync(() -> {
            log.info("Exekverar: Hämtar data från Service 2");

            return client2.get()
                    .uri("/api/test")
                    .headers(h -> h.setBearerAuth(jwtToken))
                    .retrieve()
                    .body(String.class);
        });
    }

    @SchemaMapping(typeName = "Result", field = "service3")
    public CompletableFuture<String> getService3(Result result) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        return CompletableFuture.supplyAsync(() -> {
            log.info("Exekverar: Hämtar data från Service 3 (med X-User-Name)");
            try {
                return client3.get()
                        .uri("/api/test")
                        .headers(h -> h.set("X-User-Name", auth.getName()))
                        .retrieve()
                        .body(String.class);
            } catch (Exception e) {
                return "Service 3 (ChatService) unreachable via REST";
            }
        });
    }
}

record Result(String service1, String service2, String service3) {}
record UserProfile(String username, String email, String status) {}
record MessageRequest(String recipient, String content) {}
record ChatMessage(String sender, String recipient, String content, long timestamp) {}
record HistoryResponse(java.util.List<ChatMessage> messages) {}
record ChatResponse(boolean success, String messageId) {}

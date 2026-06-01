package org.example.userservice;

import io.grpc.stub.StreamObserver;
import org.example.grpc.user.*;
import org.example.userservice.model.UserEntity;
import org.example.userservice.repository.UserRepository;
import org.springframework.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(UserGrpcService.class);
    private final UserRepository userRepository;

    public UserGrpcService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void getUserProfile(UserRequest request, StreamObserver<UserResponse> responseObserver) {
        log.info("gRPC: Fetching profile for user {}", request.getUsername());
        
        UserEntity user = userRepository.findByUsername(request.getUsername())
                .orElse(null);

        if (user != null) {
            UserResponse response = UserResponse.newBuilder()
                    .setUsername(user.getUsername())
                    .setEmail(user.getEmail())
                    .setStatus(user.getStatus())
                    .setId(user.getId())
                    .build();
            responseObserver.onNext(response);
        } else {
            // In a real app, you might return a NOT_FOUND error status
            log.warn("User {} not found via gRPC", request.getUsername());
        }
        
        responseObserver.onCompleted();
    }
}

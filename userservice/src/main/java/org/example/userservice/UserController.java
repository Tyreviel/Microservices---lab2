package org.example.userservice;

import org.example.userservice.model.UserEntity;
import org.example.userservice.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public UserProfile register(@RequestBody RegisterRequest request) {
        log.info("Registering new user: {}", request.username());
        
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        UserEntity user = new UserEntity(
            request.username(),
            passwordEncoder.encode(request.password()),
            request.email(),
            "Online"
        );
        
        UserEntity saved = userRepository.save(user);
        return new UserProfile(saved.getUsername(), saved.getEmail(), saved.getStatus());
    }

    @GetMapping("/me")
    public UserProfile getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        log.info("Fetching profile for current user: {}", username);
        
        return userRepository.findByUsername(username)
                .map(u -> new UserProfile(u.getUsername(), u.getEmail(), u.getStatus()))
                .orElse(new UserProfile(username, username + "@example.com", "Online (External)"));
    }

    @GetMapping("/{username}")
    public UserProfile getUserProfile(@PathVariable String username) {
        log.info("Fetching profile for user: {}", username);
        return userRepository.findByUsername(username)
                .map(u -> new UserProfile(u.getUsername(), u.getEmail(), u.getStatus()))
                .orElse(new UserProfile(username, "unknown", "Offline"));
    }
    
    @GetMapping("/test")
    public String test() {
        return "Hello from User Service!";
    }
}

record UserProfile(String username, String email, String status) {}
record RegisterRequest(String username, String password, String email) {}

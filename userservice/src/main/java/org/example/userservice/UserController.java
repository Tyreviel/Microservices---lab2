package org.example.userservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/me")
    public UserProfile getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("Fetching profile for current user: {}", auth.getName());
        return new UserProfile(auth.getName(), auth.getName() + "@example.com", "Online");
    }

    @GetMapping("/{username}")
    public UserProfile getUserProfile(@PathVariable String username) {
        log.info("Fetching profile for user: {}", username);
        // Mocking user lookup
        return new UserProfile(username, username + "@example.com", "Unknown");
    }
    
    @GetMapping("/test")
    public String test() {
        return "Hello from User Service!";
    }
}

record UserProfile(String username, String email, String status) {}

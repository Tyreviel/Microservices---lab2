package org.example.bff;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/home")
    public Map<String, Object> home() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Map.of(
            "status", "Successfully Logged In",
            "user", auth.getName(),
            "authorities", auth.getAuthorities(),
            "message", "Welcome to the Microservices Lab! You can use /graphiql for the GraphQL interface or /api/users/me for your profile."
        );
    }
}

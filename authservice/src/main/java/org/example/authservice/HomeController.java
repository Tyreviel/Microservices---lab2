package org.example.authservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home(Authentication auth) {
        return Map.of(
            "service", "Auth Service",
            "status", "Running",
            "user", auth != null ? auth.getName() : "Anonymous",
            "message", "This is the Authorization Server. OIDC Discovery is available at /.well-known/openid-configuration"
        );
    }
}

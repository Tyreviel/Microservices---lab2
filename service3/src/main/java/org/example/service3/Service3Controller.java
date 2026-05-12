package org.example.service3;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Service3Controller {

    @GetMapping("/api/test")
    public String getTest(@RequestHeader("X-User-Name") String username) {
        return "Hej " + username;
    }
}

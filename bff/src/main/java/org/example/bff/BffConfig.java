package org.example.bff;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class BffConfig {

    @Bean
    SecurityFilterChain security(HttpSecurity http) {
        return http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated())
                .oauth2Login(Customizer.withDefaults())
                .build();
    }

//    @Bean
//    RouterFunction<ServerResponse> router() {
//
//
//    }

}

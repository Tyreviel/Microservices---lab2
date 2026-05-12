//package org.example.service3.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
//
//@Configuration
//public class SecurityConfig {
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .addFilterBefore(new GatewayHeaderFilter(), UsernamePasswordAuthenticationFilter.class)
//                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
//        return http.build();
//    }
//
//    //Inbyggt stöd
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        RequestHeaderAuthenticationFilter filter = new RequestHeaderAuthenticationFilter();
//        filter.setPrincipalRequestHeader("X-User-Name");
//        filter.setAuthenticationManager(authentication -> {
//            // Vi litar på gatewayen, så vi returnerar bara det vi fick
//            authentication.setAuthenticated(true);
//            return authentication;
//        });
//
//        http
//                .addFilter(filter)
//                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
//
//        return http.build();
//    }
//}

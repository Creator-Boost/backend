/*package com.creatorboost.chat_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/ws/**")) // ignore CSRF for WS
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ws/**").permitAll() // allow handshake
                        .anyRequest().authenticated())
                .build();
    }
}
*/
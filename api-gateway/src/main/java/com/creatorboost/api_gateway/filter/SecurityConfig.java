package com.creatorboost.api_gateway.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtAuthenticationManager jwtAuthenticationManager;
    private final JwtServerAuthenticationConverter jwtConverter;

    public SecurityConfig(JwtAuthenticationManager jwtAuthenticationManager,
                          JwtServerAuthenticationConverter jwtConverter) {
        this.jwtAuthenticationManager = jwtAuthenticationManager;
        this.jwtConverter = jwtConverter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        AuthenticationWebFilter authWebFilter = new AuthenticationWebFilter(jwtAuthenticationManager);
        authWebFilter.setServerAuthenticationConverter(jwtConverter);

        http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .pathMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/is-authenticated",
                                "/api/auth/send-reset-otp",
                                "/api/auth/reset-password",
                                "/api/auth/verify-otp",
                                "/api/auth/send-otp",
                               // "/api/auth/**",
                                "/api/chat/ws/**"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance());

        // Apply auth filter only to protected paths
        http.addFilterAt(authWebFilter, SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }
}

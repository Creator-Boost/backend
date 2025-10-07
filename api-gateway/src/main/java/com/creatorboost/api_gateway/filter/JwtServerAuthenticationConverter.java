package com.creatorboost.api_gateway.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpCookie;

@Component
public class JwtServerAuthenticationConverter implements ServerAuthenticationConverter {

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        // First check Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String authToken = authHeader.substring(7);
            return Mono.just(new JwtAuthenticationToken(authToken));
        }

        // Then check cookies
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst("jwt");
        if (cookie != null) {
            String authToken = cookie.getValue();
            return Mono.just(new JwtAuthenticationToken(authToken));
        }

        return Mono.empty();
    }
}
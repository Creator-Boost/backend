package com.creatorboost.api_gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

/**
 * Adds X-User-Email header when an authenticated principal is present.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AddAuthenticatedUserHeaderFilter implements GlobalFilter {

    private static final String USER_HEADER = "X-User-Email";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(auth -> {
                    if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
                        ServerWebExchange mutatedExchange = exchange.mutate()
                                .request(builder -> builder.header(USER_HEADER, auth.getName()))
                                .build();
                        return chain.filter(mutatedExchange);
                    }
                    return chain.filter(exchange);
                })
                // If no security context exists, continue normally
                .switchIfEmpty(chain.filter(exchange));
    }
}

/*package com.creatorboost.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${jwt.secret.key}")
    private String SECRET_KEY;

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/oauth2/callback",
            "/api/auth/send-reset-otp",
            "/api/auth/reset-password",
            "/api/auth/profile/"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        log.info("➡️ Incoming request: [{} {}]", exchange.getRequest().getMethod(), path);

        // Log all request headers
        log.info("📌 Request Headers:");
        exchange.getRequest().getHeaders().forEach((key, values) -> log.info("   {} : {}", key, values));

        // Log all cookies
        log.info("🍪 Cookies:");
        exchange.getRequest().getCookies().forEach((name, cookieList) ->
                cookieList.forEach(cookie -> log.info("   {} : {}", name, cookie.getValue()))
        );

        // Skip public paths
        if (EXCLUDED_PATHS.stream().anyMatch(path::startsWith)) {
            log.info("🔓 Public endpoint [{}], skipping JWT validation", path);
            return chain.filter(exchange);
        }

        // Extract JWT
        String jwt = getJwtFromRequest(exchange.getRequest());

        if (jwt == null) {
            log.warn("❌ No JWT found for protected path [{}]", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            // Parse JWT
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(jwt)
                    .getBody();

            // Log all claims
            log.info("✅ JWT validated successfully for [{}]", path);
            log.info("📄 JWT Claims:");
            claims.forEach((key, value) -> log.info("   {} : {}", key, value));

            // Check expiration
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                log.warn("❌ JWT token expired for [{}]", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // Forward claims downstream
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Email", claims.getSubject())
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.error("❌ Invalid JWT token for [{}]: {}", path, e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private String getJwtFromRequest(ServerHttpRequest request) {
        // 1. Check Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.info("🔑 JWT found in Authorization header");
            return authHeader.substring(7);
        }

        // 2. Check cookies
        if (request.getCookies().containsKey("jwt")) {
            log.info("🔑 JWT found in cookie");
            return request.getCookies().getFirst("jwt").getValue();
        }

        log.warn("⚠️ JWT not found in header or cookie");
        return null;
    }
}*/

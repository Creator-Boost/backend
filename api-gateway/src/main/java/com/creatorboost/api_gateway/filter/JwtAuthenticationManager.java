package com.creatorboost.api_gateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationManager.class);

    // ⚠️ IMPORTANT: THIS SECRET MUST MATCH THE GENERATION SERVICE EXACTLY.
    // It should also be at least 32 characters long for security and HS256.
    private final String SECRET = "yoursecretkeyhereforjwttokengenerationandvalidation";

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        // Since the AuthenticationWebFilter passes the token after conversion,
        // it will be an instance of JwtAuthenticationToken.
        if (! (authentication instanceof JwtAuthenticationToken)) {
            return Mono.empty();
        }

        String authToken = authentication.getCredentials() != null
                ? authentication.getCredentials().toString()
                : null;

        logger.info("[JWT] Incoming token: {}", authToken);

        if (authToken == null || authToken.isEmpty()) {
            logger.warn("[JWT] Token is missing");
            return Mono.empty();
        }

        try {
            // ⚠️ This is where the signature mismatch error happens if the SECRET is wrong.
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken)
                    .getBody();

            logger.info("[JWT] Claims parsed: {}", claims);

            // Note: Date comparison should handle slight clock skew gracefully if possible,
            // but standard Jwts implementation handles "exp" validation.
            if (claims.getExpiration().before(new Date())) {
                logger.warn("[JWT] Token expired at: {}", claims.getExpiration());
                return Mono.empty();
            }

            String email = claims.getSubject();
            logger.info("[JWT] Token subject (email): {}", email);

            // Return an authenticated JwtAuthenticationToken
            return Mono.just(new JwtAuthenticationToken(email, authToken));
        } catch (JwtException e) {
            // Catches ExpiredJwtException, SignatureException, etc.
            logger.error("[JWT] JWT validation failed: {}", e.getMessage());
            return Mono.empty();
        } catch (Exception e) {
            logger.error("[JWT] Unexpected error: ", e);
            return Mono.empty();
        }
    }
}
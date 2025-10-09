package com.example.order_service.service;

import com.example.order_service.dto.UserProfileDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

@Service
public class AuthServiceClient {

    private final RestTemplate restTemplate;

    @Value("${auth.service.url:http://localhost:8081}")
    private String authServiceBaseUrl;

    public AuthServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Get user profile by user ID
     */
    public UserProfileDTO getUserProfile(UUID userId) {
        try {
            String url = authServiceBaseUrl + "/profile/" + userId;
            return restTemplate.getForObject(url, UserProfileDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("User not found with ID: " + userId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch user profile: " + e.getMessage(), e);
        }
    }
}

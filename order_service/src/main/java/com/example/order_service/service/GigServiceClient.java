//package com.example.order_service.service;
//
//import com.example.order_service.dto.GigWithPackageDetailsDTO;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.client.HttpClientErrorException;
//
//import java.util.Map;
//import java.util.UUID;
//
//@Service
//public class GigServiceClient {
//
//    private final RestTemplate restTemplate;
//
//    @Value("${gig.service.url:http://gig_service:8080}")
//    private String gigServiceBaseUrl;
//
//    public GigServiceClient() {
//        this.restTemplate = new RestTemplate();
//    }
//
//    /**
//     * Verify if a gig and package combination exists
//     */
//    public boolean verifyGigAndPackageExists(UUID gigId, UUID packageId) {
//        try {
//            String url = gigServiceBaseUrl + "/api/gigs/" + gigId + "/packages/" + packageId + "/verify";
//            Map<String, Boolean> response = restTemplate.getForObject(url, Map.class);
//            return response != null && response.getOrDefault("exists", false);
//        } catch (HttpClientErrorException.NotFound e) {
//            return false;
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to verify gig and package existence: " + e.getMessage(), e);
//        }
//    }
//
//    /**
//     * Get complete gig and package details from gig service
//     */
//    public GigWithPackageDetailsDTO getGigWithPackageDetails(UUID gigId, UUID packageId) {
//        try {
//            String url = gigServiceBaseUrl + "/api/gigs/" + gigId + "/packages/" + packageId;
//            return restTemplate.getForObject(url, GigWithPackageDetailsDTO.class);
//        } catch (HttpClientErrorException.NotFound e) {
//            throw new RuntimeException("Gig or package not found");
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to fetch gig and package details: " + e.getMessage(), e);
//        }
//    }
//}

package com.example.order_service.service;

import com.example.order_service.dto.GigWithPackageDetailsDTO;
import com.example.order_service.dto.GigDetailsDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;
import java.util.UUID;

@Service
public class GigServiceClient {

    private final RestTemplate restTemplate;

    @Value("${gig.service.url:http://localhost:8084}")
    private String gigServiceBaseUrl;

    public GigServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Verify if a gig and package combination exists
     */
    public boolean verifyGigAndPackageExists(UUID gigId, UUID packageId) {
        try {
            String url = gigServiceBaseUrl + "/api/gigs/" + gigId + "/packages/" + packageId + "/verify";
            Map<String, Boolean> response = restTemplate.getForObject(url, Map.class);
            return response != null && response.getOrDefault("exists", false);
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify gig and package existence: " + e.getMessage(), e);
        }
    }

    /**
     * Get complete gig and package details from gig service
     */
    public GigWithPackageDetailsDTO getGigWithPackageDetails(UUID gigId, UUID packageId) {
        try {
            String url = gigServiceBaseUrl + "/api/gigs/" + gigId + "/packages/" + packageId;
            return restTemplate.getForObject(url, GigWithPackageDetailsDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("Gig or package not found");
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch gig and package details: " + e.getMessage(), e);
        }
    }

    /**
     * Get gig details by gig ID
     */
    public GigDetailsDTO getGigDetails(UUID gigId) {
        try {
            String url = gigServiceBaseUrl + "/api/gigs/" + gigId;
            return restTemplate.getForObject(url, GigDetailsDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("Gig not found with ID: " + gigId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch gig details: " + e.getMessage(), e);
        }
    }

    /**
     * Notify gig service about a new review to update gig rating statistics
     */
    public void notifyGigReview(UUID gigId, Integer rating, String reviewText, UUID reviewerId) {
        try {
            String url = gigServiceBaseUrl + "/api/gigs/" + gigId + "/reviews";

            System.out.println("Attempting to notify gig service at: " + url);

            // Create request body with review details
            Map<String, Object> reviewData = Map.of(
                "rating", rating,
                "reviewText", reviewText,
                "reviewerId", reviewerId.toString(), // Convert UUID to string explicitly
                "createdAt", java.time.LocalDateTime.now().toString()
            );

            System.out.println("Sending review data: " + reviewData);

            Object response = restTemplate.postForObject(url, reviewData, Map.class);
            System.out.println("Gig service notification successful: " + response);

        } catch (Exception e) {
            // Log the error but don't fail the review creation
            System.err.println("Failed to notify gig service about review: " + e.getMessage());
            e.printStackTrace();
            // In a production environment, you might want to use a proper logger
            // and possibly implement a retry mechanism or queue for failed notifications
        }
    }
}

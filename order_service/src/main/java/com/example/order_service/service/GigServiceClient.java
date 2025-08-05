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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;
import java.util.UUID;

@Service
public class GigServiceClient {

    private final RestTemplate restTemplate;

    @Value("${gig.service.url:http://gig_service:8080}")
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
}

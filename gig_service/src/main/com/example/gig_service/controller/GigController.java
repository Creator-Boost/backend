package src.main.com.example.gig_service.controller;

import com.example.gig_service.dto.*;
import src.main.com.example.gig_service.service.GigService;
import src.main.com.example.gig_service.service.ImageService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/gigs")
public class GigController {

    @Autowired
    private GigService gigService;

    @Autowired
    private ImageService imageService;

    @PostMapping
    public GigResponseDTO createGig(@RequestBody GigRequestDTO gigRequestDTO) {
        return gigService.createGig(gigRequestDTO);
    }

    @GetMapping
    public List<GigResponseDTO> getAllGigs() {
        return gigService.getAllGigs();
    }

    @GetMapping("/{id}")
    public GigResponseDTO getGigById(@PathVariable UUID id) {
        return gigService.getGigById(id);
    }

    @PutMapping("/{id}")
    public GigResponseDTO updateGig(@PathVariable UUID id, @RequestBody GigRequestDTO gigRequestDTO) {
        return gigService.updateGig(id, gigRequestDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteGig(@PathVariable UUID id) {
        gigService.deleteGig(id);
    }

    @PostMapping("/create-with-images")
    public ResponseEntity<GigResponseDTO> createGigWithImages(
            @RequestParam("sellerId") UUID sellerId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("platform") String platform,
            @RequestParam("category") String category,
            @RequestParam("status") String status,
            @RequestParam(value = "images", required = false) MultipartFile[] imageFiles,
            @RequestParam(value = "primaryImageIndex", required = false, defaultValue = "0") int primaryImageIndex,
            @RequestParam(value = "packages", required = false) String packagesJson,
            @RequestParam(value = "faqs", required = false) String faqsJson) {

        try {
            GigRequestDTO gigRequestDTO = new GigRequestDTO();
            gigRequestDTO.setSellerId(sellerId);
            gigRequestDTO.setTitle(title);
            gigRequestDTO.setDescription(description);
            gigRequestDTO.setPlatform(platform);
            gigRequestDTO.setCategory(category);
            gigRequestDTO.setStatus(status);

            // Handle image uploads
            if (imageFiles != null && imageFiles.length > 0) {
                List<String> imageUrls = imageService.uploadMultipleImages(imageFiles);
                List<GigImageDTO> imageDTOs = new ArrayList<>();

                for (int i = 0; i < imageUrls.size(); i++) {
                    GigImageDTO imageDTO = new GigImageDTO();
                    imageDTO.setUrl(imageUrls.get(i));
                    imageDTO.setIsPrimary(i == primaryImageIndex);
                    imageDTOs.add(imageDTO);
                }
                gigRequestDTO.setImages(imageDTOs);
            }

            // Handle packages JSON
            if (packagesJson != null && !packagesJson.trim().isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                List<GigPackageDTO> packages = mapper.readValue(packagesJson,
                    new TypeReference<List<GigPackageDTO>>() {});
                gigRequestDTO.setPackages(packages);
            }

            // Handle FAQs JSON
            if (faqsJson != null && !faqsJson.trim().isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                List<GigFaqDTO> faqs = mapper.readValue(faqsJson,
                    new TypeReference<List<GigFaqDTO>>() {});
                gigRequestDTO.setFaqs(faqs);
            }

            GigResponseDTO response = gigService.createGig(gigRequestDTO);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = imageService.uploadImage(file);
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("message", "Image uploaded successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/upload-images")
    public ResponseEntity<Map<String, Object>> uploadImages(@RequestParam("files") MultipartFile[] files) {
        try {
            if (files.length == 0) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "No files provided");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            List<String> imageUrls = imageService.uploadMultipleImages(files);
            Map<String, Object> response = new HashMap<>();
            response.put("imageUrls", imageUrls);
            response.put("message", "Images uploaded successfully");
            response.put("count", imageUrls.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to upload images: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}

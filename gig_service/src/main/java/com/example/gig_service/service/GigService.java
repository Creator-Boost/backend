package com.example.gig_service.service;

import com.example.gig_service.dto.*;
import com.example.gig_service.entity.Gig;
import com.example.gig_service.entity.GigFaq;
import com.example.gig_service.entity.GigImage;
import com.example.gig_service.entity.GigPackage;
import com.example.gig_service.repository.GigPackageRepository;
import com.example.gig_service.repository.GigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GigService {

    @Autowired
    private GigRepository gigRepository;

    @Autowired
    private GigPackageRepository gigPackageRepository;

    public GigResponseDTO createGig(GigRequestDTO dto) {
        Gig gig = new Gig();
        gig.setSellerId(dto.getSellerId());
        gig.setTitle(dto.getTitle());
        gig.setDescription(dto.getDescription());
        gig.setPlatform(dto.getPlatform());
        gig.setCategory(dto.getCategory());
        gig.setStatus(dto.getStatus());

        // --- Map Images ---
        if (dto.getImages() != null) {
            List<GigImage> images = dto.getImages().stream().map(imgDTO -> {
                GigImage img = new GigImage();
                img.setUrl(imgDTO.getUrl());
                img.setIsPrimary(imgDTO.isIsPrimary());
                img.setGig(gig); // Important: Set parent!
                return img;
            }).collect(Collectors.toList());
            gig.setImages(images);
        }

        // --- Map Packages ---
        if (dto.getPackages() != null) {
            List<GigPackage> packages = dto.getPackages().stream().map(pkgDTO -> {
                GigPackage pkg = new GigPackage();
                pkg.setName(pkgDTO.getName());
                pkg.setPrice(pkgDTO.getPrice());
                pkg.setDeliveryDays(pkgDTO.getDeliveryDays());
                pkg.setDescription(pkgDTO.getDescription());
                pkg.setGig(gig); // Important: Set parent!
                return pkg;
            }).collect(Collectors.toList());
            gig.setPackages(packages);
        }

        // --- Map FAQs ---
        if (dto.getFaqs() != null) {
            List<GigFaq> faqs = dto.getFaqs().stream().map(faqDTO -> {
                GigFaq faq = new GigFaq();
                faq.setQuestion(faqDTO.getQuestion());
                faq.setAnswer(faqDTO.getAnswer());
                faq.setGig(gig); // Important: Set parent!
                return faq;
            }).collect(Collectors.toList());
            gig.setFaqs(faqs);
        }

        Gig savedGig = gigRepository.save(gig);
        return mapToResponseDTO(savedGig);
    }

    public List<GigResponseDTO> getAllGigs() {
        List<Gig> gigs = gigRepository.findAll();
        return gigs.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public GigResponseDTO getGigById(UUID id) {
        Gig gig = gigRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Gig not found"));
        return mapToResponseDTO(gig);
    }

    public GigResponseDTO updateGig(UUID id, GigRequestDTO dto) {
        Gig gig = gigRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Gig not found"));
        gig.setSellerId(dto.getSellerId());
        gig.setTitle(dto.getTitle());
        gig.setDescription(dto.getDescription());
        gig.setPlatform(dto.getPlatform());
        gig.setCategory(dto.getCategory());
        gig.setStatus(dto.getStatus());

        // --- Update Images ---
        gig.getImages().clear();
        if (dto.getImages() != null) {
            List<GigImage> images = dto.getImages().stream().map(imgDTO -> {
                GigImage img = new GigImage();
                img.setUrl(imgDTO.getUrl());
                img.setIsPrimary(imgDTO.isIsPrimary());
                img.setGig(gig);
                return img;
            }).collect(Collectors.toList());
            gig.getImages().addAll(images);
        }

        // --- Update Packages ---
        gig.getPackages().clear();
        if (dto.getPackages() != null) {
            List<GigPackage> packages = dto.getPackages().stream().map(pkgDTO -> {
                GigPackage pkg = new GigPackage();
                pkg.setName(pkgDTO.getName());
                pkg.setPrice(pkgDTO.getPrice());
                pkg.setDeliveryDays(pkgDTO.getDeliveryDays());
                pkg.setDescription(pkgDTO.getDescription());
                pkg.setGig(gig);
                return pkg;
            }).collect(Collectors.toList());
            gig.getPackages().addAll(packages);
        }

        // --- Update FAQs ---
        gig.getFaqs().clear();
        if (dto.getFaqs() != null) {
            List<GigFaq> faqs = dto.getFaqs().stream().map(faqDTO -> {
                GigFaq faq = new GigFaq();
                faq.setQuestion(faqDTO.getQuestion());
                faq.setAnswer(faqDTO.getAnswer());
                faq.setGig(gig);
                return faq;
            }).collect(Collectors.toList());
            gig.getFaqs().addAll(faqs);
        }

        Gig updatedGig = gigRepository.save(gig);
        return mapToResponseDTO(updatedGig);
    }

    public void deleteGig(UUID id) {
        if (!gigRepository.existsById(id)) {
            throw new NoSuchElementException("Gig not found");
        }
        gigRepository.deleteById(id);
    }

    /**
     * Get gig and specific package details by gigId and packageId
     * Verifies that both gig and package exist and that the package belongs to the gig
     */
    public GigWithPackageDetailsDTO getGigWithPackageDetails(UUID gigId, UUID packageId) {
        // First verify that the gig exists
        Gig gig = gigRepository.findById(gigId)
                .orElseThrow(() -> new NoSuchElementException("Gig not found with ID: " + gigId));

        // Verify that the package exists and belongs to the specified gig
        GigPackage gigPackage = gigPackageRepository.findByGigIdAndPackageId(gigId, packageId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Package not found with ID: " + packageId + " for gig ID: " + gigId));

        // Map to response DTO
        return mapToGigWithPackageDetailsDTO(gig, gigPackage);
    }

    /**
     * Verify if a gig and package combination exists
     */
    public boolean verifyGigAndPackageExists(UUID gigId, UUID packageId) {
        // Check if gig exists
        if (!gigRepository.existsById(gigId)) {
            return false;
        }

        // Check if package exists for the specific gig
        return gigPackageRepository.existsByGigIdAndId(gigId, packageId);
    }

    /**
     * Get all packages for a specific gig
     */
    public List<GigPackageDetailsDTO> getPackagesByGigId(UUID gigId) {
        // Verify gig exists
        if (!gigRepository.existsById(gigId)) {
            throw new NoSuchElementException("Gig not found with ID: " + gigId);
        }

        List<GigPackage> packages = gigPackageRepository.findByGigId(gigId);
        return packages.stream()
                .map(this::mapToGigPackageDetailsDTO)
                .collect(Collectors.toList());
    }

    // --- Helper: Entity → DTO ---
    private GigResponseDTO mapToResponseDTO(Gig gig) {
        GigResponseDTO dto = new GigResponseDTO();
        dto.setId(gig.getId());
        dto.setSellerId(gig.getSellerId());
        dto.setTitle(gig.getTitle());
        dto.setDescription(gig.getDescription());
        dto.setPlatform(gig.getPlatform());
        dto.setCategory(gig.getCategory());
        dto.setStatus(gig.getStatus());
        dto.setCreatedAt(gig.getCreatedAt());
        dto.setUpdatedAt(gig.getUpdatedAt());

        // --- Map Images ---
        if (gig.getImages() != null) {
            List<GigImageDTO> images = gig.getImages().stream().map(img -> {
                GigImageDTO imgDTO = new GigImageDTO();
                imgDTO.setUrl(img.getUrl());
                imgDTO.setIsPrimary(img.isIsPrimary());
                return imgDTO;
            }).collect(Collectors.toList());
            dto.setImages(images);
        }

        // --- Map Packages ---
        if (gig.getPackages() != null) {
            List<GigPackageDTO> packages = gig.getPackages().stream().map(pkg -> {
                GigPackageDTO pkgDTO = new GigPackageDTO();
                pkgDTO.setName(pkg.getName());
                pkgDTO.setPrice(pkg.getPrice());
                pkgDTO.setDeliveryDays(pkg.getDeliveryDays());
                pkgDTO.setDescription(pkg.getDescription());
                return pkgDTO;
            }).collect(Collectors.toList());
            dto.setPackages(packages);
        }

        // --- Map FAQs ---
        if (gig.getFaqs() != null) {
            List<GigFaqDTO> faqs = gig.getFaqs().stream().map(faq -> {
                GigFaqDTO faqDTO = new GigFaqDTO();
                faqDTO.setQuestion(faq.getQuestion());
                faqDTO.setAnswer(faq.getAnswer());
                return faqDTO;
            }).collect(Collectors.toList());
            dto.setFaqs(faqs);
        }

        return dto;
    }

    // Helper method to map Gig and GigPackage to GigWithPackageDetailsDTO
    private GigWithPackageDetailsDTO mapToGigWithPackageDetailsDTO(Gig gig, GigPackage selectedPackage) {
        GigWithPackageDetailsDTO dto = new GigWithPackageDetailsDTO();

        // Map gig details
        dto.setGigId(gig.getId());
        dto.setSellerId(gig.getSellerId());
        dto.setTitle(gig.getTitle());
        dto.setDescription(gig.getDescription());
        dto.setPlatform(gig.getPlatform());
        dto.setCategory(gig.getCategory());
        dto.setStatus(gig.getStatus());
        dto.setCreatedAt(gig.getCreatedAt());
        dto.setUpdatedAt(gig.getUpdatedAt());

        // Map images
        if (gig.getImages() != null) {
            List<GigImageDTO> images = gig.getImages().stream().map(img -> {
                GigImageDTO imgDTO = new GigImageDTO();
                imgDTO.setUrl(img.getUrl());
                imgDTO.setIsPrimary(img.isIsPrimary());
                return imgDTO;
            }).collect(Collectors.toList());
            dto.setImages(images);
        }

        // Map FAQs
        if (gig.getFaqs() != null) {
            List<GigFaqDTO> faqs = gig.getFaqs().stream().map(faq -> {
                GigFaqDTO faqDTO = new GigFaqDTO();
                faqDTO.setQuestion(faq.getQuestion());
                faqDTO.setAnswer(faq.getAnswer());
                return faqDTO;
            }).collect(Collectors.toList());
            dto.setFaqs(faqs);
        }

        // Map all packages
        if (gig.getPackages() != null) {
            List<GigPackageDTO> allPackages = gig.getPackages().stream().map(pkg -> {
                GigPackageDTO pkgDTO = new GigPackageDTO();
                pkgDTO.setName(pkg.getName());
                pkgDTO.setPrice(pkg.getPrice());
                pkgDTO.setDeliveryDays(pkg.getDeliveryDays());
                pkgDTO.setDescription(pkg.getDescription());
                return pkgDTO;
            }).collect(Collectors.toList());
            dto.setAllPackages(allPackages);
        }

        // Map selected package details
        dto.setSelectedPackage(mapToGigPackageDetailsDTO(selectedPackage));

        return dto;
    }

    // Helper method to map GigPackage to GigPackageDetailsDTO
    private GigPackageDetailsDTO mapToGigPackageDetailsDTO(GigPackage gigPackage) {
        GigPackageDetailsDTO dto = new GigPackageDetailsDTO();
        dto.setPackageId(gigPackage.getId());
        dto.setGigId(gigPackage.getGig().getId());
        dto.setName(gigPackage.getName());
        dto.setPrice(gigPackage.getPrice());
        dto.setDeliveryDays(gigPackage.getDeliveryDays());
        dto.setDescription(gigPackage.getDescription());
        return dto;
    }
}
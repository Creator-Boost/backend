package com.example.gig_service.service;

import com.example.gig_service.dto.*;
import com.example.gig_service.entity.Gig;
import com.example.gig_service.entity.GigFaq;
import com.example.gig_service.entity.GigImage;
import com.example.gig_service.entity.GigPackage;
import com.example.gig_service.repository.GigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GigServiceTest {

    @Mock
    private GigRepository gigRepository;

    @InjectMocks
    private GigService gigService;

    private UUID testGigId;
    private UUID testSellerId;
    private Gig testGig;
    private GigRequestDTO testGigRequestDTO;
    private GigResponseDTO testGigResponseDTO;

    @BeforeEach
    void setUp() {
        testGigId = UUID.randomUUID();
        testSellerId = UUID.randomUUID();

        // Setup test entities
        testGig = createTestGig();
        testGigRequestDTO = createTestGigRequestDTO();
        testGigResponseDTO = createTestGigResponseDTO();
    }

    @Test
    void createGig_WithValidData_ShouldReturnGigResponseDTO() {
        // Arrange
        Gig savedGig = createTestGig();
        savedGig.setId(testGigId);
        when(gigRepository.save(any(Gig.class))).thenReturn(savedGig);

        // Act
        GigResponseDTO result = gigService.createGig(testGigRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testGigRequestDTO.getTitle(), result.getTitle());
        assertEquals(testGigRequestDTO.getDescription(), result.getDescription());
        assertEquals(testGigRequestDTO.getSellerId(), result.getSellerId());
        assertEquals(testGigRequestDTO.getPlatform(), result.getPlatform());
        assertEquals(testGigRequestDTO.getCategory(), result.getCategory());
        assertEquals(testGigRequestDTO.getStatus(), result.getStatus());

        verify(gigRepository, times(1)).save(any(Gig.class));
    }

    @Test
    void createGig_WithImagesPackagesAndFaqs_ShouldMapCorrectly() {
        // Arrange
        Gig savedGig = createTestGigWithAllFields();
        savedGig.setId(testGigId);
        when(gigRepository.save(any(Gig.class))).thenReturn(savedGig);

        // Act
        GigResponseDTO result = gigService.createGig(testGigRequestDTO);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getImages());
        assertFalse(result.getImages().isEmpty());
        assertNotNull(result.getPackages());
        assertFalse(result.getPackages().isEmpty());
        assertNotNull(result.getFaqs());
        assertFalse(result.getFaqs().isEmpty());

        verify(gigRepository, times(1)).save(any(Gig.class));
    }

    @Test
    void getAllGigs_WhenGigsExist_ShouldReturnListOfGigResponseDTO() {
        // Arrange
        List<Gig> gigs = Arrays.asList(testGig, createTestGig());
        when(gigRepository.findAll()).thenReturn(gigs);

        // Act
        List<GigResponseDTO> result = gigService.getAllGigs();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(gigRepository, times(1)).findAll();
    }

    @Test
    void getAllGigs_WhenNoGigsExist_ShouldReturnEmptyList() {
        // Arrange
        when(gigRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<GigResponseDTO> result = gigService.getAllGigs();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(gigRepository, times(1)).findAll();
    }

    @Test
    void getGigById_WhenGigExists_ShouldReturnGigResponseDTO() {
        // Arrange
        when(gigRepository.findById(testGigId)).thenReturn(Optional.of(testGig));

        // Act
        GigResponseDTO result = gigService.getGigById(testGigId);

        // Assert
        assertNotNull(result);
        assertEquals(testGig.getTitle(), result.getTitle());
        assertEquals(testGig.getDescription(), result.getDescription());
        verify(gigRepository, times(1)).findById(testGigId);
    }

    @Test
    void getGigById_WhenGigDoesNotExist_ShouldThrowNoSuchElementException() {
        // Arrange
        when(gigRepository.findById(testGigId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            gigService.getGigById(testGigId);
        });

        verify(gigRepository, times(1)).findById(testGigId);
    }

    @Test
    void updateGig_WhenGigExists_ShouldReturnUpdatedGigResponseDTO() {
        // Arrange
        Gig existingGig = createTestGig();
        existingGig.setId(testGigId);

        GigRequestDTO updateRequest = createTestGigRequestDTO();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription("Updated Description");

        when(gigRepository.findById(testGigId)).thenReturn(Optional.of(existingGig));
        when(gigRepository.save(any(Gig.class))).thenReturn(existingGig);

        // Act
        GigResponseDTO result = gigService.updateGig(testGigId, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        verify(gigRepository, times(1)).findById(testGigId);
        verify(gigRepository, times(1)).save(any(Gig.class));
    }

    @Test
    void updateGig_WhenGigDoesNotExist_ShouldThrowNoSuchElementException() {
        // Arrange
        when(gigRepository.findById(testGigId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            gigService.updateGig(testGigId, testGigRequestDTO);
        });

        verify(gigRepository, times(1)).findById(testGigId);
        verify(gigRepository, never()).save(any(Gig.class));
    }

    @Test
    void deleteGig_WhenGigExists_ShouldDeleteSuccessfully() {
        // Arrange
        when(gigRepository.existsById(testGigId)).thenReturn(true);
        doNothing().when(gigRepository).deleteById(testGigId);

        // Act
        assertDoesNotThrow(() -> {
            gigService.deleteGig(testGigId);
        });

        // Assert
        verify(gigRepository, times(1)).existsById(testGigId);
        verify(gigRepository, times(1)).deleteById(testGigId);
    }

    @Test
    void deleteGig_WhenGigDoesNotExist_ShouldThrowNoSuchElementException() {
        // Arrange
        when(gigRepository.existsById(testGigId)).thenReturn(false);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            gigService.deleteGig(testGigId);
        });

        verify(gigRepository, times(1)).existsById(testGigId);
        verify(gigRepository, never()).deleteById(testGigId);
    }

    // Helper methods
    private Gig createTestGig() {
        Gig gig = new Gig();
        gig.setId(testGigId);
        gig.setSellerId(testSellerId);
        gig.setTitle("Test Gig");
        gig.setDescription("Test Description");
        gig.setPlatform("Test Platform");
        gig.setCategory("Test Category");
        gig.setStatus("ACTIVE");
        gig.setImages(new ArrayList<>());
        gig.setPackages(new ArrayList<>());
        gig.setFaqs(new ArrayList<>());
        return gig;
    }

    private Gig createTestGigWithAllFields() {
        Gig gig = createTestGig();

        // Add test image
        GigImage image = new GigImage();
        image.setUrl("http://test-image.com");
        image.setIsPrimary(true);
        image.setGig(gig);
        gig.getImages().add(image);

        // Add test package
        GigPackage pkg = new GigPackage();
        pkg.setName("Basic Package");
        pkg.setPrice(new BigDecimal("50.00"));
        pkg.setDeliveryDays(5);
        pkg.setDescription("Basic package description");
        pkg.setGig(gig);
        gig.getPackages().add(pkg);

        // Add test FAQ
        GigFaq faq = new GigFaq();
        faq.setQuestion("Test Question?");
        faq.setAnswer("Test Answer");
        faq.setGig(gig);
        gig.getFaqs().add(faq);

        return gig;
    }

    private GigRequestDTO createTestGigRequestDTO() {
        GigRequestDTO dto = new GigRequestDTO();
        dto.setSellerId(testSellerId);
        dto.setTitle("Test Gig");
        dto.setDescription("Test Description");
        dto.setPlatform("Test Platform");
        dto.setCategory("Test Category");
        dto.setStatus("ACTIVE");

        // Add test image DTO
        GigImageDTO imageDTO = new GigImageDTO();
        imageDTO.setUrl("http://test-image.com");
        imageDTO.setIsPrimary(true);
        dto.setImages(Arrays.asList(imageDTO));

        // Add test package DTO
        GigPackageDTO packageDTO = new GigPackageDTO();
        packageDTO.setName("Basic Package");
        packageDTO.setPrice(new BigDecimal("50.00"));
        packageDTO.setDeliveryDays(5);
        packageDTO.setDescription("Basic package description");
        dto.setPackages(Arrays.asList(packageDTO));

        // Add test FAQ DTO
        GigFaqDTO faqDTO = new GigFaqDTO();
        faqDTO.setQuestion("Test Question?");
        faqDTO.setAnswer("Test Answer");
        dto.setFaqs(Arrays.asList(faqDTO));

        return dto;
    }

    private GigResponseDTO createTestGigResponseDTO() {
        GigResponseDTO dto = new GigResponseDTO();
        dto.setId(testGigId);
        dto.setSellerId(testSellerId);
        dto.setTitle("Test Gig");
        dto.setDescription("Test Description");
        dto.setPlatform("Test Platform");
        dto.setCategory("Test Category");
        dto.setStatus("ACTIVE");
        dto.setImages(new ArrayList<>());
        dto.setPackages(new ArrayList<>());
        dto.setFaqs(new ArrayList<>());
        return dto;
    }
}

//# Run all tests
//.\mvnw.cmd test
//
//# Run only specific test class
//.\mvnw.cmd test -Dtest=GigServiceTest
//
//# Run tests with verbose output
//.\mvnw.cmd test -X
//
//# Run tests and generate coverage report (if configured)
//.\mvnw.cmd test jacoco:report
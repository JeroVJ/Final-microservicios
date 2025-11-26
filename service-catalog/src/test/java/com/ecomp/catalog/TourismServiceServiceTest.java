package com.ecomp.catalog;

import com.ecomp.catalog.dto.CatalogDtos.*;
import com.ecomp.catalog.entity.TourismService;
import com.ecomp.catalog.repository.TourismServiceRepository;
import com.ecomp.catalog.service.TourismServiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TourismServiceServiceTest {

    @Autowired
    private TourismServiceService service;

    @Autowired
    private TourismServiceRepository repository;

    private static final String TEST_PROVIDER_ID = "provider-123";

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void createService_Success() {
        ServiceInput input = ServiceInput.builder()
                .name("Eco Lodge Test")
                .description("A beautiful eco lodge")
                .price(BigDecimal.valueOf(150.00))
                .category("Alojamiento")
                .city("Leticia")
                .countryCode("CO")
                .build();

        ServiceDto result = service.create(TEST_PROVIDER_ID, input);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Eco Lodge Test", result.getName());
        assertEquals("Alojamiento", result.getCategory());
        assertEquals(TEST_PROVIDER_ID, result.getProviderId());
    }

    @Test
    void getById_Found() {
        TourismService saved = repository.save(TourismService.builder()
                .providerId(TEST_PROVIDER_ID)
                .name("Test Service")
                .description("Test Description")
                .price(BigDecimal.valueOf(100.00))
                .category("Transporte")
                .city("Bogotá")
                .build());

        Optional<ServiceDto> result = service.getById(saved.getId());

        assertTrue(result.isPresent());
        assertEquals("Test Service", result.get().getName());
    }

    @Test
    void getById_NotFound() {
        Optional<ServiceDto> result = service.getById(UUID.randomUUID());
        assertTrue(result.isEmpty());
    }

    @Test
    void search_ByName() {
        repository.save(TourismService.builder()
                .providerId(TEST_PROVIDER_ID)
                .name("Eco Lodge Amazon")
                .category("Alojamiento")
                .build());
        repository.save(TourismService.builder()
                .providerId(TEST_PROVIDER_ID)
                .name("City Hotel")
                .category("Alojamiento")
                .build());

        List<ServiceDto> results = service.search("Amazon");

        assertEquals(1, results.size());
        assertEquals("Eco Lodge Amazon", results.get(0).getName());
    }

    @Test
    void search_ByCategory() {
        repository.save(TourismService.builder()
                .providerId(TEST_PROVIDER_ID)
                .name("Service 1")
                .category("Transporte")
                .build());
        repository.save(TourismService.builder()
                .providerId(TEST_PROVIDER_ID)
                .name("Service 2")
                .category("Alojamiento")
                .build());

        List<ServiceDto> results = service.search("Transporte");

        assertEquals(1, results.size());
    }

    @Test
    void updateService_Success() {
        TourismService saved = repository.save(TourismService.builder()
                .providerId(TEST_PROVIDER_ID)
                .name("Original Name")
                .price(BigDecimal.valueOf(100.00))
                .build());

        ServiceInput updateInput = ServiceInput.builder()
                .name("Updated Name")
                .price(BigDecimal.valueOf(150.00))
                .city("Medellín")
                .build();

        Optional<ServiceDto> result = service.update(saved.getId(), TEST_PROVIDER_ID, updateInput);

        assertTrue(result.isPresent());
        assertEquals("Updated Name", result.get().getName());
        assertEquals(BigDecimal.valueOf(150.00), result.get().getPrice());
        assertEquals("Medellín", result.get().getCity());
    }

    @Test
    void updateService_WrongProvider() {
        TourismService saved = repository.save(TourismService.builder()
                .providerId(TEST_PROVIDER_ID)
                .name("Original Name")
                .build());

        ServiceInput updateInput = ServiceInput.builder()
                .name("Updated Name")
                .build();

        Optional<ServiceDto> result = service.update(saved.getId(), "wrong-provider", updateInput);

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteService_Success() {
        TourismService saved = repository.save(TourismService.builder()
                .providerId(TEST_PROVIDER_ID)
                .name("To Delete")
                .build());

        boolean deleted = service.delete(saved.getId(), TEST_PROVIDER_ID);

        assertTrue(deleted);
        assertFalse(repository.existsById(saved.getId()));
    }

    @Test
    void deleteService_WrongProvider() {
        TourismService saved = repository.save(TourismService.builder()
                .providerId(TEST_PROVIDER_ID)
                .name("To Delete")
                .build());

        boolean deleted = service.delete(saved.getId(), "wrong-provider");

        assertFalse(deleted);
        assertTrue(repository.existsById(saved.getId()));
    }

    @Test
    void getByProvider_ReturnsOnlyProviderServices() {
        repository.save(TourismService.builder()
                .providerId(TEST_PROVIDER_ID)
                .name("Provider 1 Service")
                .build());
        repository.save(TourismService.builder()
                .providerId("other-provider")
                .name("Other Provider Service")
                .build());

        List<ServiceDto> results = service.getByProvider(TEST_PROVIDER_ID);

        assertEquals(1, results.size());
        assertEquals("Provider 1 Service", results.get(0).getName());
    }

    @Test
    void askQuestion_Success() {
        TourismService saved = repository.save(TourismService.builder()
                .providerId(TEST_PROVIDER_ID)
                .name("Service with Questions")
                .build());

        Optional<ServiceQuestionDto> result = service.askQuestion(
                saved.getId(), 
                "user-123", 
                "Is breakfast included?"
        );

        assertTrue(result.isPresent());
        assertEquals("Is breakfast included?", result.get().getQuestion());
        assertNull(result.get().getAnswer());
    }

    @Test
    void answerQuestion_Success() {
        TourismService saved = repository.save(TourismService.builder()
                .providerId(TEST_PROVIDER_ID)
                .name("Service")
                .build());

        ServiceQuestionDto question = service.askQuestion(
                saved.getId(), 
                "user-123", 
                "Question?"
        ).orElseThrow();

        Optional<ServiceQuestionDto> answered = service.answerQuestion(
                UUID.fromString(question.getId()),
                TEST_PROVIDER_ID,
                "Answer!"
        );

        assertTrue(answered.isPresent());
        assertEquals("Answer!", answered.get().getAnswer());
        assertNotNull(answered.get().getAnsweredAt());
    }

    @Test
    void updateRating_Success() {
        TourismService saved = repository.save(TourismService.builder()
                .providerId(TEST_PROVIDER_ID)
                .name("Service")
                .rating(BigDecimal.ZERO)
                .ratingCount(0)
                .build());

        service.updateRating(saved.getId(), BigDecimal.valueOf(5));
        service.updateRating(saved.getId(), BigDecimal.valueOf(3));

        TourismService updated = repository.findById(saved.getId()).orElseThrow();
        assertEquals(2, updated.getRatingCount());
        assertEquals(BigDecimal.valueOf(4.0).setScale(1), updated.getRating());
    }
}

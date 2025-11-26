package com.ecomp.user;

import com.ecomp.user.dto.UserDtos.*;
import com.ecomp.user.entity.UserProfile;
import com.ecomp.user.repository.UserProfileRepository;
import com.ecomp.user.service.UserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserProfileServiceTest {

    @Autowired
    private UserProfileService service;

    @Autowired
    private UserProfileRepository repository;

    private static final String TEST_KEYCLOAK_ID = "test-keycloak-123";

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void createProfile_Success() {
        UserProfileInput input = UserProfileInput.builder()
                .username("testuser")
                .email("test@example.com")
                .age(25)
                .description("Test description")
                .role("CLIENT")
                .build();

        UserProfileDto result = service.createOrUpdate(TEST_KEYCLOAK_ID, input);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(25, result.getAge());
        assertEquals("CLIENT", result.getRole());
    }

    @Test
    void updateProfile_Success() {
        // First create
        UserProfileInput createInput = UserProfileInput.builder()
                .username("testuser")
                .email("test@example.com")
                .role("CLIENT")
                .build();
        service.createOrUpdate(TEST_KEYCLOAK_ID, createInput);

        // Then update
        UserProfileInput updateInput = UserProfileInput.builder()
                .username("updateduser")
                .email("updated@example.com")
                .age(30)
                .role("PROVIDER")
                .phone("+1234567890")
                .build();

        UserProfileDto result = service.createOrUpdate(TEST_KEYCLOAK_ID, updateInput);

        assertNotNull(result);
        assertEquals("updateduser", result.getUsername());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals(30, result.getAge());
        assertEquals("PROVIDER", result.getRole());
        assertEquals("+1234567890", result.getPhone());
    }

    @Test
    void getByKeycloakId_Found() {
        UserProfileInput input = UserProfileInput.builder()
                .username("testuser")
                .email("test@example.com")
                .role("CLIENT")
                .build();
        service.createOrUpdate(TEST_KEYCLOAK_ID, input);

        Optional<UserProfileDto> result = service.getByKeycloakId(TEST_KEYCLOAK_ID);

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void getByKeycloakId_NotFound() {
        Optional<UserProfileDto> result = service.getByKeycloakId("non-existent-id");

        assertTrue(result.isEmpty());
    }

    @Test
    void getByUsername_Found() {
        UserProfileInput input = UserProfileInput.builder()
                .username("uniqueuser")
                .email("unique@example.com")
                .role("CLIENT")
                .build();
        service.createOrUpdate(TEST_KEYCLOAK_ID, input);

        Optional<UserProfileDto> result = service.getByUsername("uniqueuser");

        assertTrue(result.isPresent());
        assertEquals("unique@example.com", result.get().getEmail());
    }

    @Test
    void delete_Success() {
        UserProfileInput input = UserProfileInput.builder()
                .username("todelete")
                .email("delete@example.com")
                .role("CLIENT")
                .build();
        service.createOrUpdate(TEST_KEYCLOAK_ID, input);

        service.delete(TEST_KEYCLOAK_ID);

        Optional<UserProfile> deleted = repository.findByKeycloakId(TEST_KEYCLOAK_ID);
        assertTrue(deleted.isEmpty());
    }

    @Test
    void createProviderProfile_WithExtraFields() {
        UserProfileInput input = UserProfileInput.builder()
                .username("provider1")
                .email("provider@example.com")
                .age(35)
                .description("Tourism service provider")
                .role("PROVIDER")
                .phone("+573001234567")
                .website("https://ecoturismo.com")
                .socialMedia("@ecoturismo")
                .build();

        UserProfileDto result = service.createOrUpdate(TEST_KEYCLOAK_ID, input);

        assertNotNull(result);
        assertEquals("PROVIDER", result.getRole());
        assertEquals("+573001234567", result.getPhone());
        assertEquals("https://ecoturismo.com", result.getWebsite());
        assertEquals("@ecoturismo", result.getSocialMedia());
    }
}

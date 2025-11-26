package com.ecomp.user.service;

import com.ecomp.user.dto.UserDtos.*;
import com.ecomp.user.entity.UserProfile;
import com.ecomp.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserProfileRepository repository;

    @Transactional(readOnly = true)
    public Optional<UserProfileDto> getByKeycloakId(String keycloakId) {
        log.info("Getting user profile for keycloakId: {}", keycloakId);
        return repository.findByKeycloakId(keycloakId)
                .map(UserProfileDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Optional<UserProfileDto> getByUsername(String username) {
        return repository.findByUsername(username)
                .map(UserProfileDto::fromEntity);
    }

    @Transactional
    public UserProfileDto createOrUpdate(String keycloakId, UserProfileInput input) {
        log.info("Creating or updating user profile for keycloakId: {}", keycloakId);
        
        UserProfile profile = repository.findByKeycloakId(keycloakId)
                .map(existing -> {
                    existing.setUsername(input.getUsername());
                    existing.setEmail(input.getEmail());
                    existing.setAge(input.getAge());
                    existing.setPhotoBase64(input.getPhotoBase64());
                    existing.setDescription(input.getDescription());
                    if (input.getRole() != null) {
                        existing.setRole(UserProfile.UserRole.valueOf(input.getRole()));
                    }
                    existing.setPhone(input.getPhone());
                    existing.setWebsite(input.getWebsite());
                    existing.setSocialMedia(input.getSocialMedia());
                    return existing;
                })
                .orElseGet(() -> UserProfile.builder()
                        .keycloakId(keycloakId)
                        .username(input.getUsername())
                        .email(input.getEmail())
                        .age(input.getAge())
                        .photoBase64(input.getPhotoBase64())
                        .description(input.getDescription())
                        .role(input.getRole() != null ? UserProfile.UserRole.valueOf(input.getRole()) : UserProfile.UserRole.CLIENT)
                        .phone(input.getPhone())
                        .website(input.getWebsite())
                        .socialMedia(input.getSocialMedia())
                        .build());

        UserProfile saved = repository.save(profile);
        log.info("User profile saved with id: {}", saved.getId());
        return UserProfileDto.fromEntity(saved);
    }

    @Transactional
    public void delete(String keycloakId) {
        log.info("Deleting user profile for keycloakId: {}", keycloakId);
        repository.findByKeycloakId(keycloakId)
                .ifPresent(repository::delete);
    }
}

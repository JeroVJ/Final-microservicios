package com.ecomp.user.controller;

import com.ecomp.user.dto.UserDtos.*;
import com.ecomp.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserProfileController {

    private final UserProfileService service;

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        String keycloakId = jwt.getSubject();
        log.info("Getting current user profile for: {}", keycloakId);
        
        return service.getByKeycloakId(keycloakId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserProfileDto> getByUsername(@PathVariable String username) {
        log.info("Getting user profile for username: {}", username);
        
        return service.getByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/profile")
    public ResponseEntity<UserProfileDto> createOrUpdate(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserProfileInput input) {
        
        String keycloakId = jwt.getSubject();
        log.info("Creating/updating profile for: {}", keycloakId);
        
        UserProfileDto result = service.createOrUpdate(keycloakId, input);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        String keycloakId = jwt.getSubject();
        log.info("Deleting user profile for: {}", keycloakId);
        
        service.delete(keycloakId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User service is healthy");
    }
}

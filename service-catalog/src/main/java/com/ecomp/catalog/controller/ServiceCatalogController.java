package com.ecomp.catalog.controller;

import com.ecomp.catalog.dto.CatalogDtos.*;
import com.ecomp.catalog.service.TourismServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ServiceCatalogController {

    private final TourismServiceService service;

    @GetMapping
    public ResponseEntity<List<ServiceDto>> getAll(@RequestParam(required = false) String filter) {
        log.info("Getting services with filter: {}", filter);
        List<ServiceDto> services = filter != null && !filter.isEmpty() 
                ? service.search(filter) 
                : service.getAll();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceDto> getById(@PathVariable String id) {
        log.info("Getting service by id: {}", id);
        return service.getById(UUID.fromString(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ServiceDto>> getByCategory(@PathVariable String category) {
        log.info("Getting services by category: {}", category);
        return ResponseEntity.ok(service.getByCategory(category));
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<ServiceDto>> getByProvider(@PathVariable String providerId) {
        log.info("Getting services by provider: {}", providerId);
        return ResponseEntity.ok(service.getByProvider(providerId));
    }

    @GetMapping("/my-services")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<List<ServiceDto>> getMyServices(@AuthenticationPrincipal Jwt jwt) {
        String providerId = jwt.getSubject();
        log.info("Getting services for provider: {}", providerId);
        return ResponseEntity.ok(service.getByProvider(providerId));
    }

    @PostMapping
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ServiceDto> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ServiceInput input) {
        String providerId = jwt.getSubject();
        log.info("Creating service for provider: {}", providerId);
        
        ServiceDto created = service.create(providerId, input);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ServiceDto> update(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ServiceInput input) {
        String providerId = jwt.getSubject();
        log.info("Updating service {} for provider: {}", id, providerId);
        
        return service.update(UUID.fromString(id), providerId, input)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Void> delete(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {
        String providerId = jwt.getSubject();
        log.info("Deleting service {} for provider: {}", id, providerId);
        
        if (service.delete(UUID.fromString(id), providerId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Service Catalog is healthy");
    }
}

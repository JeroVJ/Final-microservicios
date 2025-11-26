package com.ecomp.review.controller;

import com.ecomp.review.dto.ReviewDtos.*;
import com.ecomp.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<ReviewDto>> getByServiceId(@PathVariable String serviceId) {
        log.info("Getting reviews for service: {}", serviceId);
        return ResponseEntity.ok(reviewService.getByServiceId(UUID.fromString(serviceId)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewDto>> getByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(reviewService.getByUserId(userId));
    }

    @GetMapping("/my-reviews")
    public ResponseEntity<List<ReviewDto>> getMyReviews(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(reviewService.getByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> getById(@PathVariable String id) {
        return reviewService.getById(UUID.fromString(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats/{serviceId}")
    public ResponseEntity<ReviewStats> getStats(@PathVariable String serviceId) {
        return ResponseEntity.ok(reviewService.getStats(UUID.fromString(serviceId)));
    }

    @PostMapping
    public ResponseEntity<ReviewDto> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ReviewInput input) {
        String userId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");
        
        log.info("Creating review for service {} by user {}", input.getServiceId(), userId);
        
        try {
            ReviewDto review = reviewService.create(userId, username, input);
            return ResponseEntity.status(HttpStatus.CREATED).body(review);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto> update(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ReviewInput input) {
        String userId = jwt.getSubject();
        
        return reviewService.update(UUID.fromString(id), userId, input)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        
        if (reviewService.delete(UUID.fromString(id), userId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Review service is healthy");
    }
}

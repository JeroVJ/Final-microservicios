package com.ecomp.review.service;

import com.ecomp.review.dto.ReviewDtos.*;
import com.ecomp.review.entity.Review;
import com.ecomp.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository repository;

    @Transactional(readOnly = true)
    public List<ReviewDto> getByServiceId(UUID serviceId) {
        log.info("Getting reviews for service: {}", serviceId);
        return repository.findByServiceIdOrderByCreatedAtDesc(serviceId).stream()
                .map(ReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getByUserId(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ReviewDto> getById(UUID id) {
        return repository.findById(id)
                .map(ReviewDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public ReviewStats getStats(UUID serviceId) {
        Double avgRating = repository.getAverageRatingByServiceId(serviceId);
        Long total = repository.countByServiceId(serviceId);
        
        return ReviewStats.builder()
                .serviceId(serviceId.toString())
                .averageRating(avgRating != null ? avgRating : 0.0)
                .totalReviews(total)
                .fiveStars(repository.countByServiceIdAndRating(serviceId, 5))
                .fourStars(repository.countByServiceIdAndRating(serviceId, 4))
                .threeStars(repository.countByServiceIdAndRating(serviceId, 3))
                .twoStars(repository.countByServiceIdAndRating(serviceId, 2))
                .oneStar(repository.countByServiceIdAndRating(serviceId, 1))
                .build();
    }

    @Transactional
    public ReviewDto create(String userId, String username, ReviewInput input) {
        UUID serviceId = UUID.fromString(input.getServiceId());
        
        // Check if user already reviewed this service
        if (repository.existsByServiceIdAndUserId(serviceId, userId)) {
            throw new IllegalStateException("User has already reviewed this service");
        }

        log.info("Creating review for service {} by user {}", serviceId, userId);
        
        Review review = Review.builder()
                .serviceId(serviceId)
                .userId(userId)
                .username(username)
                .rating(input.getRating())
                .comment(input.getComment())
                .build();

        Review saved = repository.save(review);
        log.info("Review created: {}", saved.getId());
        
        return ReviewDto.fromEntity(saved);
    }

    @Transactional
    public Optional<ReviewDto> update(UUID reviewId, String userId, ReviewInput input) {
        return repository.findById(reviewId)
                .filter(r -> r.getUserId().equals(userId))
                .map(review -> {
                    review.setRating(input.getRating());
                    review.setComment(input.getComment());
                    Review saved = repository.save(review);
                    log.info("Review updated: {}", saved.getId());
                    return ReviewDto.fromEntity(saved);
                });
    }

    @Transactional
    public boolean delete(UUID reviewId, String userId) {
        return repository.findById(reviewId)
                .filter(r -> r.getUserId().equals(userId))
                .map(review -> {
                    repository.delete(review);
                    log.info("Review deleted: {}", reviewId);
                    return true;
                })
                .orElse(false);
    }
}

package com.ecomp.review.dto;

import com.ecomp.review.entity.Review;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ReviewDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewDto {
        private String id;
        private String serviceId;
        private String userId;
        private String username;
        private Integer rating;
        private String comment;
        private LocalDateTime createdAt;

        public static ReviewDto fromEntity(Review entity) {
            if (entity == null) return null;
            return ReviewDto.builder()
                    .id(entity.getId().toString())
                    .serviceId(entity.getServiceId().toString())
                    .userId(entity.getUserId())
                    .username(entity.getUsername())
                    .rating(entity.getRating())
                    .comment(entity.getComment())
                    .createdAt(entity.getCreatedAt())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewInput {
        @NotBlank(message = "Service ID is required")
        private String serviceId;
        
        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        private Integer rating;
        
        private String comment;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewStats {
        private String serviceId;
        private Double averageRating;
        private Long totalReviews;
        private Long fiveStars;
        private Long fourStars;
        private Long threeStars;
        private Long twoStars;
        private Long oneStar;
    }
}

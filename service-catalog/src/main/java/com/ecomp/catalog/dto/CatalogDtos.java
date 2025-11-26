package com.ecomp.catalog.dto;

import com.ecomp.catalog.entity.ServiceImage;
import com.ecomp.catalog.entity.ServiceQuestion;
import com.ecomp.catalog.entity.TourismService;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CatalogDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceDto {
        private String id;
        private String providerId;
        private String name;
        private String description;
        private BigDecimal price;
        private String category;
        private String city;
        private String countryCode;
        private BigDecimal rating;
        private Integer ratingCount;
        private Double latitude;
        private Double longitude;
        private String transportType;
        private LocalDateTime departureTime;
        private LocalDateTime arrivalTime;
        private String routeDescription;
        private List<ServiceImageDto> images;
        private List<ServiceQuestionDto> questions;
        private CountryInfoDto countryInfo;
        private WeatherInfoDto weatherInfo;

        public static ServiceDto fromEntity(TourismService entity) {
            if (entity == null) return null;
            return ServiceDto.builder()
                    .id(entity.getId().toString())
                    .providerId(entity.getProviderId())
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .price(entity.getPrice())
                    .category(entity.getCategory())
                    .city(entity.getCity())
                    .countryCode(entity.getCountryCode())
                    .rating(entity.getRating())
                    .ratingCount(entity.getRatingCount())
                    .latitude(entity.getLatitude())
                    .longitude(entity.getLongitude())
                    .transportType(entity.getTransportType())
                    .departureTime(entity.getDepartureTime())
                    .arrivalTime(entity.getArrivalTime())
                    .routeDescription(entity.getRouteDescription())
                    .images(entity.getImages() != null ? 
                            entity.getImages().stream().map(ServiceImageDto::fromEntity).collect(Collectors.toList()) : 
                            List.of())
                    .questions(entity.getQuestions() != null ? 
                            entity.getQuestions().stream().map(ServiceQuestionDto::fromEntity).collect(Collectors.toList()) : 
                            List.of())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceInput {
        @NotBlank(message = "Name is required")
        private String name;
        private String description;
        private BigDecimal price;
        private String category;
        private String city;
        private String countryCode;
        private Double latitude;
        private Double longitude;
        private String transportType;
        private LocalDateTime departureTime;
        private LocalDateTime arrivalTime;
        private String routeDescription;
        private List<String> imageUrls;

        public TourismService toEntity(String providerId) {
            return TourismService.builder()
                    .providerId(providerId)
                    .name(this.name)
                    .description(this.description)
                    .price(this.price)
                    .category(this.category)
                    .city(this.city)
                    .countryCode(this.countryCode)
                    .latitude(this.latitude)
                    .longitude(this.longitude)
                    .transportType(this.transportType)
                    .departureTime(this.departureTime)
                    .arrivalTime(this.arrivalTime)
                    .routeDescription(this.routeDescription)
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceImageDto {
        private String id;
        private String imageUrl;
        private String imageBase64;
        private Boolean isPrimary;

        public static ServiceImageDto fromEntity(ServiceImage entity) {
            if (entity == null) return null;
            return ServiceImageDto.builder()
                    .id(entity.getId().toString())
                    .imageUrl(entity.getImageUrl())
                    .imageBase64(entity.getImageBase64())
                    .isPrimary(entity.getIsPrimary())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceQuestionDto {
        private String id;
        private String userId;
        private String question;
        private String answer;
        private LocalDateTime answeredAt;
        private LocalDateTime createdAt;

        public static ServiceQuestionDto fromEntity(ServiceQuestion entity) {
            if (entity == null) return null;
            return ServiceQuestionDto.builder()
                    .id(entity.getId().toString())
                    .userId(entity.getUserId())
                    .question(entity.getQuestion())
                    .answer(entity.getAnswer())
                    .answeredAt(entity.getAnsweredAt())
                    .createdAt(entity.getCreatedAt())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionInput {
        private String serviceId;
        @NotBlank(message = "Question is required")
        private String question;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CountryInfoDto {
        private String name;
        private String capital;
        private String region;
        private Long population;
        private String currency;
        private String flag;
        private List<String> languages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeatherInfoDto {
        private String description;
        private Double temperature;
        private Double humidity;
        private Double windSpeed;
        private String icon;
    }
}

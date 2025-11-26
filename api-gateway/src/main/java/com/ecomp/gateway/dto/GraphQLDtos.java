package com.ecomp.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class GraphQLDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Service {
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
        private List<ServiceImage> images;
        private List<ServiceQuestion> questions;
        private List<Review> reviews;
        private CountryInfo countryInfo;
        private WeatherInfo weatherInfo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceImage {
        private String id;
        private String imageUrl;
        private String imageBase64;
        private Boolean isPrimary;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceQuestion {
        private String id;
        private String userId;
        private String question;
        private String answer;
        private LocalDateTime answeredAt;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Review {
        private String id;
        private String serviceId;
        private String userId;
        private String username;
        private Integer rating;
        private String comment;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfile {
        private String keycloakId;
        private String username;
        private String email;
        private Integer age;
        private String photoBase64;
        private String description;
        private String role;
        private String phone;
        private String website;
        private String socialMedia;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItem {
        private String id;
        private String serviceId;
        private String serviceName;
        private String serviceCategory;
        private Integer quantity;
        private BigDecimal unitPrice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CountryInfo {
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
    public static class WeatherInfo {
        private String description;
        private Double temperature;
        private Double humidity;
        private Double windSpeed;
        private String icon;
    }

    // Input types
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceInput {
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
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfileInput {
        private String username;
        private String email;
        private Integer age;
        private String photoBase64;
        private String description;
        private String role;
        private String phone;
        private String website;
        private String socialMedia;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewInput {
        private String serviceId;
        private Integer rating;
        private String comment;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionInput {
        private String serviceId;
        private String question;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterInput {
        private String username;
        private String email;
        private String password;
        private String firstName;
        private String lastName;
    }
}

package com.ecomp.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "services", schema = "catalog")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourismService {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    private String category;
    private String city;
    
    @Column(name = "country_code")
    private String countryCode;

    @Column(precision = 2, scale = 1)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "rating_count")
    @Builder.Default
    private Integer ratingCount = 0;

    private Double latitude;

    private Double longitude;

    @Column(name = "transport_type")
    private String transportType;

    @Column(name = "departure_time")
    private LocalDateTime departureTime;

    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;

    @Column(name = "route_description", columnDefinition = "TEXT")
    private String routeDescription;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ServiceImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ServiceQuestion> questions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addImage(ServiceImage image) {
        images.add(image);
        image.setService(this);
    }

    public void addQuestion(ServiceQuestion question) {
        questions.add(question);
        question.setService(this);
    }

    public void updateRating(BigDecimal newRating) {
        BigDecimal totalRating = this.rating.multiply(BigDecimal.valueOf(this.ratingCount));
        this.ratingCount++;
        this.rating = totalRating.add(newRating).divide(BigDecimal.valueOf(this.ratingCount), 1, java.math.RoundingMode.HALF_UP);
    }
}

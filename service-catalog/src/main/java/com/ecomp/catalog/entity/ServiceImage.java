package com.ecomp.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "service_images", schema = "catalog")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    @ToString.Exclude
    private TourismService service;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_base64", columnDefinition = "TEXT")
    private String imageBase64;

    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

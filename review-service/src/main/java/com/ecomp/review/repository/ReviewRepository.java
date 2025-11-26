package com.ecomp.review.repository;

import com.ecomp.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByServiceIdOrderByCreatedAtDesc(UUID serviceId);
    
    List<Review> findByUserIdOrderByCreatedAtDesc(String userId);
    
    Optional<Review> findByServiceIdAndUserId(UUID serviceId, String userId);
    
    boolean existsByServiceIdAndUserId(UUID serviceId, String userId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.serviceId = :serviceId")
    Double getAverageRatingByServiceId(UUID serviceId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.serviceId = :serviceId")
    Long countByServiceId(UUID serviceId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.serviceId = :serviceId AND r.rating = :rating")
    Long countByServiceIdAndRating(UUID serviceId, Integer rating);
}

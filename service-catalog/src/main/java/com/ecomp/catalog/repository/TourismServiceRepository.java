package com.ecomp.catalog.repository;

import com.ecomp.catalog.entity.TourismService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TourismServiceRepository extends JpaRepository<TourismService, UUID> {

    List<TourismService> findByProviderId(String providerId);

    List<TourismService> findByCategory(String category);

    List<TourismService> findByCity(String city);

    @Query("SELECT s FROM TourismService s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
           "LOWER(s.category) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
           "LOWER(s.city) LIKE LOWER(CONCAT('%', :filter, '%'))")
    List<TourismService> searchByFilter(@Param("filter") String filter);

    @Query("SELECT s FROM TourismService s ORDER BY s.rating DESC")
    List<TourismService> findTopRated();
}

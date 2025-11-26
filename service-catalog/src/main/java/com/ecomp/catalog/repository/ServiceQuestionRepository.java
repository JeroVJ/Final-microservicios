package com.ecomp.catalog.repository;

import com.ecomp.catalog.entity.ServiceQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceQuestionRepository extends JpaRepository<ServiceQuestion, UUID> {
    List<ServiceQuestion> findByServiceId(UUID serviceId);
    List<ServiceQuestion> findByUserId(String userId);
}

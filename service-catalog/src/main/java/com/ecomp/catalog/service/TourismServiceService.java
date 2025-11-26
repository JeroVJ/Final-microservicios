package com.ecomp.catalog.service;

import com.ecomp.catalog.client.ExternalApiClient;
import com.ecomp.catalog.dto.CatalogDtos.*;
import com.ecomp.catalog.entity.ServiceQuestion;
import com.ecomp.catalog.entity.TourismService;
import com.ecomp.catalog.repository.ServiceQuestionRepository;
import com.ecomp.catalog.repository.TourismServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TourismServiceService {

    private final TourismServiceRepository repository;
    private final ServiceQuestionRepository questionRepository;
    private final ExternalApiClient externalApiClient;

    @Transactional(readOnly = true)
    public List<ServiceDto> getAll() {
        return repository.findAll().stream()
                .map(ServiceDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceDto> search(String filter) {
        if (filter == null || filter.isEmpty()) {
            return getAll();
        }
        return repository.searchByFilter(filter).stream()
                .map(ServiceDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ServiceDto> getById(UUID id) {
        return repository.findById(id)
                .map(service -> {
                    ServiceDto dto = ServiceDto.fromEntity(service);
                    
                    // Enrich with external API data
                    if (service.getCountryCode() != null) {
                        externalApiClient.getCountryInfo(service.getCountryCode())
                                .subscribe(countryInfo -> dto.setCountryInfo(countryInfo));
                    }
                    
                    if (service.getCity() != null) {
                        externalApiClient.getWeatherInfo(service.getCity(), service.getCountryCode())
                                .subscribe(weatherInfo -> dto.setWeatherInfo(weatherInfo));
                    }
                    
                    return dto;
                });
    }

    @Transactional(readOnly = true)
    public List<ServiceDto> getByProvider(String providerId) {
        return repository.findByProviderId(providerId).stream()
                .map(ServiceDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceDto> getByCategory(String category) {
        return repository.findByCategory(category).stream()
                .map(ServiceDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ServiceDto create(String providerId, ServiceInput input) {
        log.info("Creating service for provider: {}", providerId);
        
        TourismService service = input.toEntity(providerId);
        TourismService saved = repository.save(service);
        
        log.info("Service created with id: {}", saved.getId());
        return ServiceDto.fromEntity(saved);
    }

    @Transactional
    public Optional<ServiceDto> update(UUID id, String providerId, ServiceInput input) {
        return repository.findById(id)
                .filter(s -> s.getProviderId().equals(providerId))
                .map(existing -> {
                    existing.setName(input.getName());
                    existing.setDescription(input.getDescription());
                    existing.setPrice(input.getPrice());
                    existing.setCategory(input.getCategory());
                    existing.setCity(input.getCity());
                    existing.setCountryCode(input.getCountryCode());
                    existing.setLatitude(input.getLatitude());
                    existing.setLongitude(input.getLongitude());
                    existing.setTransportType(input.getTransportType());
                    existing.setDepartureTime(input.getDepartureTime());
                    existing.setArrivalTime(input.getArrivalTime());
                    existing.setRouteDescription(input.getRouteDescription());
                    
                    TourismService saved = repository.save(existing);
                    log.info("Service updated: {}", saved.getId());
                    return ServiceDto.fromEntity(saved);
                });
    }

    @Transactional
    public boolean delete(UUID id, String providerId) {
        return repository.findById(id)
                .filter(s -> s.getProviderId().equals(providerId))
                .map(service -> {
                    repository.delete(service);
                    log.info("Service deleted: {}", id);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public void updateRating(UUID serviceId, BigDecimal newRating) {
        repository.findById(serviceId).ifPresent(service -> {
            service.updateRating(newRating);
            repository.save(service);
            log.info("Rating updated for service {}: {}", serviceId, service.getRating());
        });
    }

    // Question methods
    @Transactional
    public Optional<ServiceQuestionDto> askQuestion(UUID serviceId, String userId, String question) {
        return repository.findById(serviceId)
                .map(service -> {
                    ServiceQuestion q = ServiceQuestion.builder()
                            .service(service)
                            .userId(userId)
                            .question(question)
                            .build();
                    service.addQuestion(q);
                    repository.save(service);
                    
                    log.info("Question added to service {}", serviceId);
                    return ServiceQuestionDto.fromEntity(q);
                });
    }

    @Transactional
    public Optional<ServiceQuestionDto> answerQuestion(UUID questionId, String providerId, String answer) {
        return questionRepository.findById(questionId)
                .filter(q -> q.getService().getProviderId().equals(providerId))
                .map(question -> {
                    question.setAnswer(answer);
                    question.setAnsweredAt(LocalDateTime.now());
                    questionRepository.save(question);
                    
                    log.info("Question {} answered", questionId);
                    return ServiceQuestionDto.fromEntity(question);
                });
    }
}

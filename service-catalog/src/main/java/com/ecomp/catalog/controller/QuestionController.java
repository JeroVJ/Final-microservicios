package com.ecomp.catalog.controller;

import com.ecomp.catalog.dto.CatalogDtos.*;
import com.ecomp.catalog.service.TourismServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class QuestionController {

    private final TourismServiceService service;

    @PostMapping
    public ResponseEntity<ServiceQuestionDto> askQuestion(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody QuestionInput input) {
        String userId = jwt.getSubject();
        log.info("User {} asking question on service {}", userId, input.getServiceId());
        
        return service.askQuestion(UUID.fromString(input.getServiceId()), userId, input.getQuestion())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{questionId}/answer")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ServiceQuestionDto> answerQuestion(
            @PathVariable String questionId,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody String answer) {
        String providerId = jwt.getSubject();
        log.info("Provider {} answering question {}", providerId, questionId);
        
        return service.answerQuestion(UUID.fromString(questionId), providerId, answer)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

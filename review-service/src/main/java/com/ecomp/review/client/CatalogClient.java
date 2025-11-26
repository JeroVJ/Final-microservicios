package com.ecomp.review.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
@Slf4j
public class CatalogClient {

    private final WebClient webClient;

    @Value("${services.catalog.url:http://service-catalog:8085}")
    private String catalogServiceUrl;

    public CatalogClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<Void> updateServiceRating(String serviceId, BigDecimal newRating) {
        return webClient.put()
                .uri(catalogServiceUrl + "/services/" + serviceId + "/rating")
                .bodyValue(newRating)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(e -> {
                    log.error("Error updating service rating for {}: {}", serviceId, e.getMessage());
                    return Mono.empty();
                });
    }
}

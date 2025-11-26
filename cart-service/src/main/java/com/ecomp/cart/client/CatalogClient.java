package com.ecomp.cart.client;

import com.ecomp.cart.dto.CartDtos.ServiceInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@Component
@Slf4j
public class CatalogClient {

    private final WebClient webClient;

    @Value("${services.catalog.url:http://service-catalog:8085}")
    private String catalogServiceUrl;

    public CatalogClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @SuppressWarnings("unchecked")
    public Mono<ServiceInfo> getServiceInfo(String serviceId) {
        return webClient.get()
                .uri(catalogServiceUrl + "/services/" + serviceId)
                .retrieve()
                .bodyToMono(Map.class)
                .map(map -> ServiceInfo.builder()
                        .id((String) map.get("id"))
                        .name((String) map.get("name"))
                        .category((String) map.get("category"))
                        .price(map.get("price") != null ? 
                                new BigDecimal(map.get("price").toString()) : BigDecimal.ZERO)
                        .build())
                .onErrorResume(e -> {
                    log.error("Error fetching service info for {}: {}", serviceId, e.getMessage());
                    return Mono.empty();
                });
    }
}

package com.ecomp.gateway.client;

import com.ecomp.gateway.dto.GraphQLDtos.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
public class MicroserviceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.catalog.url:http://service-catalog:8085}")
    private String catalogServiceUrl;

    @Value("${services.user.url:http://user-service:8084}")
    private String userServiceUrl;

    @Value("${services.cart.url:http://cart-service:8086}")
    private String cartServiceUrl;

    @Value("${services.review.url:http://review-service:8087}")
    private String reviewServiceUrl;

    public MicroserviceClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    // ==================== CATALOG SERVICE ====================

    public Flux<Service> getServices(String filter) {
        return webClientBuilder.build()
                .get()
                .uri(catalogServiceUrl + "/api/services" + (filter != null ? "?filter=" + filter : ""))
                .retrieve()
                .bodyToFlux(Service.class)
                .onErrorResume(e -> {
                    log.error("Error fetching services: {}", e.getMessage());
                    return Flux.empty();
                });
    }

    public Mono<Service> getServiceById(String id) {
        return webClientBuilder.build()
                .get()
                .uri(catalogServiceUrl + "/api/services/" + id)
                .retrieve()
                .bodyToMono(Service.class)
                .onErrorResume(e -> {
                    log.error("Error fetching service {}: {}", id, e.getMessage());
                    return Mono.empty();
                });
    }

    public Mono<Service> createService(ServiceInput input, String token) {
        return webClientBuilder.build()
                .post()
                .uri(catalogServiceUrl + "/api/services")
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(input)
                .retrieve()
                .bodyToMono(Service.class);
    }

    public Mono<Service> updateService(String id, ServiceInput input, String token) {
        return webClientBuilder.build()
                .put()
                .uri(catalogServiceUrl + "/api/services/" + id)
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(input)
                .retrieve()
                .bodyToMono(Service.class);
    }

    public Mono<Boolean> deleteService(String id, String token) {
        return webClientBuilder.build()
                .delete()
                .uri(catalogServiceUrl + "/api/services/" + id)
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(Void.class)
                .thenReturn(true)
                .onErrorReturn(false);
    }

    // ==================== USER SERVICE ====================

    public Mono<UserProfile> getCurrentUserProfile(String token) {
        return webClientBuilder.build()
                .get()
                .uri(userServiceUrl + "/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(UserProfile.class)
                .onErrorResume(e -> Mono.empty());
    }

    public Mono<UserProfile> createOrUpdateUserProfile(UserProfileInput input, String token) {
        return webClientBuilder.build()
                .post()
                .uri(userServiceUrl + "/api/users/profile")
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(input)
                .retrieve()
                .bodyToMono(UserProfile.class);
    }

    // ==================== CART SERVICE ====================

    public Flux<CartItem> getMyCart(String token) {
        return webClientBuilder.build()
                .get()
                .uri(cartServiceUrl + "/api/cart")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToFlux(CartItem.class)
                .onErrorResume(e -> Flux.empty());
    }

    public Mono<BigDecimal> getCartTotal(String token) {
        return webClientBuilder.build()
                .get()
                .uri(cartServiceUrl + "/api/cart/total")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(BigDecimal.class)
                .onErrorReturn(BigDecimal.ZERO);
    }

    public Mono<CartItem> addToCart(String serviceId, Integer quantity, String token) {
        return webClientBuilder.build()
                .post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("cart-service")
                        .port(8086)
                        .path("/api/cart/items")
                        .queryParam("serviceId", serviceId)
                        .queryParam("quantity", quantity != null ? quantity : 1)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(CartItem.class);
    }

    public Mono<CartItem> updateCartItemQuantity(String cartItemId, Integer quantity, String token) {
        return webClientBuilder.build()
                .put()
                .uri(cartServiceUrl + "/api/cart/items/" + cartItemId + "?quantity=" + quantity)
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(CartItem.class);
    }

    public Mono<Boolean> removeFromCart(String cartItemId, String token) {
        return webClientBuilder.build()
                .delete()
                .uri(cartServiceUrl + "/api/cart/items/" + cartItemId)
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(Void.class)
                .thenReturn(true)
                .onErrorReturn(false);
    }

    public Mono<Boolean> clearCart(String token) {
        return webClientBuilder.build()
                .delete()
                .uri(cartServiceUrl + "/api/cart")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(Void.class)
                .thenReturn(true)
                .onErrorReturn(false);
    }

    public Mono<Boolean> checkout(String token) {
        return webClientBuilder.build()
                .post()
                .uri(cartServiceUrl + "/api/cart/checkout")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(Void.class)
                .thenReturn(true)
                .onErrorReturn(false);
    }

    // ==================== REVIEW SERVICE ====================

    public Flux<Review> getReviewsByService(String serviceId) {
        return webClientBuilder.build()
                .get()
                .uri(reviewServiceUrl + "/api/reviews/service/" + serviceId)
                .retrieve()
                .bodyToFlux(Review.class)
                .onErrorResume(e -> Flux.empty());
    }

    public Mono<Review> createReview(ReviewInput input, String token) {
        return webClientBuilder.build()
                .post()
                .uri(reviewServiceUrl + "/api/reviews")
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(input)
                .retrieve()
                .bodyToMono(Review.class);
    }

    // ==================== QUESTIONS ====================

    public Mono<ServiceQuestion> askQuestion(QuestionInput input, String token) {
        return webClientBuilder.build()
                .post()
                .uri(catalogServiceUrl + "/api/questions")
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(input)
                .retrieve()
                .bodyToMono(ServiceQuestion.class);
    }

    public Mono<ServiceQuestion> answerQuestion(String questionId, String answer, String token) {
        return webClientBuilder.build()
                .put()
                .uri(catalogServiceUrl + "/api/questions/" + questionId + "/answer")
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(answer)
                .retrieve()
                .bodyToMono(ServiceQuestion.class);
    }
}

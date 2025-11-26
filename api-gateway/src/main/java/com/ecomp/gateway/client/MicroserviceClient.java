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

    @Value("${services.auth.url:http://auth-service:8083}")
    private String authServiceUrl;

    public MicroserviceClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    // ==================== CATALOG SERVICE ====================

    public Flux<Service> getServices(String filter) {
        String url = catalogServiceUrl + "/services" + (filter != null && !filter.isEmpty() ? "?filter=" + filter : "");
        log.info("Calling catalog-service at: {}", url);
        return webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Service.class)
                .doOnError(e -> log.error("Error fetching services: {}", e.getMessage()))
                .onErrorResume(e -> Flux.empty());
    }

    public Mono<Service> getServiceById(String id) {
        log.info("Calling catalog-service at: {}/services/{}", catalogServiceUrl, id);
        return webClientBuilder.build()
                .get()
                .uri(catalogServiceUrl + "/services/" + id)
                .retrieve()
                .bodyToMono(Service.class)
                .doOnError(e -> log.error("Error fetching service {}: {}", id, e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    public Mono<Service> createService(ServiceInput input, String token) {
        log.info("Calling catalog-service to create service");
        return webClientBuilder.build()
                .post()
                .uri(catalogServiceUrl + "/services")
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(input)
                .retrieve()
                .bodyToMono(Service.class)
                .doOnError(e -> log.error("Error creating service: {}", e.getMessage()));
    }

    public Mono<Service> updateService(String id, ServiceInput input, String token) {
        return webClientBuilder.build()
                .put()
                .uri(catalogServiceUrl + "/services/" + id)
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(input)
                .retrieve()
                .bodyToMono(Service.class);
    }

    public Mono<Boolean> deleteService(String id, String token) {
        return webClientBuilder.build()
                .delete()
                .uri(catalogServiceUrl + "/services/" + id)
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(Void.class)
                .thenReturn(true)
                .onErrorReturn(false);
    }

    // ==================== USER SERVICE ====================

    public Mono<UserProfile> getCurrentUserProfile(String token) {
        log.info("Calling user-service at: {}/users/me", userServiceUrl);
        return webClientBuilder.build()
                .get()
                .uri(userServiceUrl + "/users/me")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(UserProfile.class)
                .doOnError(e -> log.error("Error getting user profile: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    public Mono<UserProfile> createOrUpdateUserProfile(UserProfileInput input, String token) {
        log.info("Calling user-service at: {}/users/profile", userServiceUrl);
        return webClientBuilder.build()
                .post()
                .uri(userServiceUrl + "/users/profile")
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(input)
                .retrieve()
                .bodyToMono(UserProfile.class)
                .doOnError(e -> log.error("Error creating/updating user profile: {}", e.getMessage()));
    }

    // ==================== CART SERVICE ====================

    public Flux<CartItem> getMyCart(String token) {
        log.info("Calling cart-service at: {}/cart", cartServiceUrl);
        return webClientBuilder.build()
                .get()
                .uri(cartServiceUrl + "/cart")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToFlux(CartItem.class)
                .doOnError(e -> log.error("Error getting cart: {}", e.getMessage()))
                .onErrorResume(e -> Flux.empty());
    }

    public Mono<BigDecimal> getCartTotal(String token) {
        return webClientBuilder.build()
                .get()
                .uri(cartServiceUrl + "/cart/total")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(BigDecimal.class)
                .onErrorReturn(BigDecimal.ZERO);
    }

    public Mono<CartItem> addToCart(String serviceId, Integer quantity, String token) {
        log.info("Adding to cart: serviceId={}, quantity={}", serviceId, quantity);
        return webClientBuilder.build()
                .post()
                .uri(cartServiceUrl + "/cart/items?serviceId=" + serviceId + "&quantity=" + (quantity != null ? quantity : 1))
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(CartItem.class)
                .doOnError(e -> log.error("Error adding to cart: {}", e.getMessage()));
    }

    public Mono<CartItem> updateCartItemQuantity(String cartItemId, Integer quantity, String token) {
        return webClientBuilder.build()
                .put()
                .uri(cartServiceUrl + "/cart/items/" + cartItemId + "?quantity=" + quantity)
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(CartItem.class);
    }

    public Mono<Boolean> removeFromCart(String cartItemId, String token) {
        return webClientBuilder.build()
                .delete()
                .uri(cartServiceUrl + "/cart/items/" + cartItemId)
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(Void.class)
                .thenReturn(true)
                .onErrorReturn(false);
    }

    public Mono<Boolean> clearCart(String token) {
        return webClientBuilder.build()
                .delete()
                .uri(cartServiceUrl + "/cart")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(Void.class)
                .thenReturn(true)
                .onErrorReturn(false);
    }

    public Mono<Boolean> checkout(String token) {
        return webClientBuilder.build()
                .post()
                .uri(cartServiceUrl + "/cart/checkout")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(Void.class)
                .thenReturn(true)
                .onErrorReturn(false);
    }

    // ==================== REVIEW SERVICE ====================

    public Flux<Review> getReviewsByService(String serviceId) {
        log.info("Calling review-service for serviceId: {}", serviceId);
        return webClientBuilder.build()
                .get()
                .uri(reviewServiceUrl + "/reviews/service/" + serviceId)
                .retrieve()
                .bodyToFlux(Review.class)
                .doOnError(e -> log.error("Error getting reviews: {}", e.getMessage()))
                .onErrorResume(e -> Flux.empty());
    }

    public Mono<Review> createReview(ReviewInput input, String token) {
        log.info("Creating review for serviceId: {}", input.getServiceId());
        return webClientBuilder.build()
                .post()
                .uri(reviewServiceUrl + "/reviews")
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(input)
                .retrieve()
                .bodyToMono(Review.class)
                .doOnError(e -> log.error("Error creating review: {}", e.getMessage()));
    }

    // ==================== QUESTIONS ====================

    public Mono<ServiceQuestion> askQuestion(QuestionInput input, String token) {
        log.info("Asking question for serviceId: {}", input.getServiceId());
        return webClientBuilder.build()
                .post()
                .uri(catalogServiceUrl + "/questions")
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(input)
                .retrieve()
                .bodyToMono(ServiceQuestion.class)
                .doOnError(e -> log.error("Error asking question: {}", e.getMessage()));
    }

    public Mono<ServiceQuestion> answerQuestion(String questionId, String answer, String token) {
        return webClientBuilder.build()
                .put()
                .uri(catalogServiceUrl + "/questions/" + questionId + "/answer")
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(answer)
                .retrieve()
                .bodyToMono(ServiceQuestion.class);
    }

    // ==================== AUTH SERVICE ====================

    public Mono<Boolean> register(RegisterInput input) {
        log.info("Registering user: {}", input.getUsername());
        return webClientBuilder.build()
                .post()
                .uri(authServiceUrl + "/auth/register")
                .bodyValue(input)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> true)
                .doOnError(e -> log.error("Error registering user: {}", e.getMessage()))
                .onErrorReturn(false);
    }
}

package com.ecomp.gateway.resolver;

import com.ecomp.gateway.client.MicroserviceClient;
import com.ecomp.gateway.dto.GraphQLDtos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class GraphQLResolver {

    private final MicroserviceClient client;

    // ==================== QUERIES ====================

    @QueryMapping
    public Flux<Service> services(@Argument String filter) {
        log.info("GraphQL Query: services with filter={}", filter);
        return client.getServices(filter);
    }

    @QueryMapping
    public Mono<Service> serviceById(@Argument String id) {
        log.info("GraphQL Query: serviceById with id={}", id);
        return client.getServiceById(id);
    }

    @QueryMapping
    public Mono<UserProfile> currentUserProfile(@AuthenticationPrincipal Jwt jwt) {
        log.info("GraphQL Query: currentUserProfile");
        String token = "Bearer " + jwt.getTokenValue();
        return client.getCurrentUserProfile(token);
    }

    @QueryMapping
    public Flux<CartItem> myCart(@AuthenticationPrincipal Jwt jwt) {
        log.info("GraphQL Query: myCart");
        String token = "Bearer " + jwt.getTokenValue();
        return client.getMyCart(token);
    }

    @QueryMapping
    public Mono<BigDecimal> cartTotal(@AuthenticationPrincipal Jwt jwt) {
        log.info("GraphQL Query: cartTotal");
        String token = "Bearer " + jwt.getTokenValue();
        return client.getCartTotal(token);
    }

    @QueryMapping
    public Flux<Review> reviewsByService(@Argument String serviceId) {
        log.info("GraphQL Query: reviewsByService with serviceId={}", serviceId);
        return client.getReviewsByService(serviceId);
    }

    // ==================== MUTATIONS ====================

    @MutationMapping
    public Mono<Boolean> register(@Argument RegisterInput input) {
        log.info("GraphQL Mutation: register user {}", input.getUsername());
        return client.register(input);
    }

    @MutationMapping
    public Mono<Service> createService(@Argument ServiceInput input, @AuthenticationPrincipal Jwt jwt) {
        log.info("GraphQL Mutation: createService");
        String token = "Bearer " + jwt.getTokenValue();
        return client.createService(input, token);
    }

    @MutationMapping
    public Mono<Service> updateService(@Argument String id, @Argument ServiceInput input, @AuthenticationPrincipal Jwt jwt) {
        log.info("GraphQL Mutation: updateService with id={}", id);
        String token = "Bearer " + jwt.getTokenValue();
        return client.updateService(id, input, token);
    }

    @MutationMapping
    public Mono<Boolean> deleteService(@Argument String id, @AuthenticationPrincipal Jwt jwt) {
        log.info("GraphQL Mutation: deleteService with id={}", id);
        String token = "Bearer " + jwt.getTokenValue();
        return client.deleteService(id, token);
    }

    @MutationMapping
    public Mono<UserProfile> createOrUpdateUserProfile(@Argument UserProfileInput input, @AuthenticationPrincipal Jwt jwt) {
        log.info("GraphQL Mutation: createOrUpdateUserProfile");
        String token = "Bearer " + jwt.getTokenValue();
        return client.createOrUpdateUserProfile(input, token);
    }

    @MutationMapping
    public Mono<CartItem> addToCart(@Argument String serviceId, @Argument Integer quantity, @AuthenticationPrincipal Jwt jwt) {
        log.info("GraphQL Mutation: addToCart serviceId={}, quantity={}", serviceId, quantity);
        String token = "Bearer " + jwt.getTokenValue();
        return client.addToCart(serviceId, quantity, token);
    }

    @MutationMapping
    public Mono<CartItem> updateCartItemQuantity(@Argument String cartItemId, @Argument Integer quantity, @AuthenticationPrincipal Jwt jwt) {
        log.info("GraphQL Mutation: updateCartItemQuantity cartItemId={}, quantity={}", cartItemId, quantity);
        String token = "Bearer " + jwt.getTokenValue();
        return client.updateCartItemQuantity(cartItemId, quantity, token);
    }

    @MutationMapping
    public Mono<Boolean> removeFromCart(@Argument String cartItemId, @AuthenticationPrincipal Jwt jwt) {
        log.info("GraphQL Mutation: removeFromCart cartItemId={}", cartItemId);
        String token = "Bearer " + jwt.getTokenValue();
        return client.removeFromCart(cartItemId, token);
    }

    @MutationMapping
    public Mono<Boolean> clearCart(@AuthenticationPrincipal Jwt jwt) {
        log.info("GraphQL Mutation: clearCart");
        String token = "Bearer " + jwt.getTokenValue();
        return client.clearCart(token);
    }

    @MutationMapping
    public Mono<Boolean> checkoutCart(@AuthenticationPrincipal Jwt jwt) {
        log.info("GraphQL Mutation: checkoutCart");
        String token = "Bearer " + jwt.getTokenValue();
        return client.checkout(token);
    }

    @MutationMapping
    public Mono<Review> createReview(@Argument ReviewInput input, @AuthenticationPrincipal Jwt jwt) {
        log.info("GraphQL Mutation: createReview serviceId={}", input.getServiceId());
        String token = "Bearer " + jwt.getTokenValue();
        return client.createReview(input, token);
    }

    @MutationMapping
    public Mono<ServiceQuestion> askQuestion(@Argument QuestionInput input, @AuthenticationPrincipal Jwt jwt) {
        log.info("GraphQL Mutation: askQuestion serviceId={}", input.getServiceId());
        String token = "Bearer " + jwt.getTokenValue();
        return client.askQuestion(input, token);
    }

    @MutationMapping
    public Mono<ServiceQuestion> answerQuestion(@Argument String questionId, @Argument String answer, @AuthenticationPrincipal Jwt jwt) {
        log.info("GraphQL Mutation: answerQuestion questionId={}", questionId);
        String token = "Bearer " + jwt.getTokenValue();
        return client.answerQuestion(questionId, answer, token);
    }
}

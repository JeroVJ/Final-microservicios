package com.ecomp.auth.service;

import com.ecomp.auth.dto.AuthDtos.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class KeycloakAuthService {

    private final WebClient webClient;

    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.admin-username}")
    private String adminUsername;

    @Value("${keycloak.admin-password}")
    private String adminPassword;

    public KeycloakAuthService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<TokenResponse> login(LoginRequest request) {
        String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        return webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("username", request.getUsername())
                        .with("password", request.getPassword()))
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::mapToTokenResponse)
                .doOnError(e -> log.error("Login failed for user {}: {}", request.getUsername(), e.getMessage()));
    }

    public Mono<TokenResponse> refreshToken(String refreshToken) {
        String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        return webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("refresh_token", refreshToken))
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::mapToTokenResponse)
                .doOnError(e -> log.error("Token refresh failed: {}", e.getMessage()));
    }

    public Mono<Void> logout(String refreshToken) {
        String logoutUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/logout";

        return webClient.post()
                .uri(logoutUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("refresh_token", refreshToken))
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> log.error("Logout failed: {}", e.getMessage()));
    }

    @SuppressWarnings("unchecked")
    public Mono<UserInfo> getUserInfo(String accessToken) {
        String userInfoUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/userinfo";

        return webClient.get()
                .uri(userInfoUrl)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(map -> UserInfo.builder()
                        .id((String) map.get("sub"))
                        .username((String) map.get("preferred_username"))
                        .email((String) map.get("email"))
                        .firstName((String) map.get("given_name"))
                        .lastName((String) map.get("family_name"))
                        .roles((List<String>) ((Map<String, Object>) map.getOrDefault("realm_access", Map.of())).getOrDefault("roles", List.of()))
                        .build())
                .doOnError(e -> log.error("Get user info failed: {}", e.getMessage()));
    }

    public Mono<Boolean> register(RegisterRequest request) {
        log.info("Starting registration for user: {}", request.getUsername());
        return getAdminToken()
                .flatMap(adminToken -> {
                    log.info("Got admin token, creating user...");
                    return createUser(adminToken, request);
                })
                .doOnSuccess(result -> log.info("Registration result for {}: {}", request.getUsername(), result))
                .doOnError(e -> log.error("Registration failed for {}: {}", request.getUsername(), e.getMessage()));
    }

    private Mono<String> getAdminToken() {
        String tokenUrl = keycloakUrl + "/realms/master/protocol/openid-connect/token";
        log.debug("Getting admin token from: {}", tokenUrl);

        return webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", "admin-cli")
                        .with("username", adminUsername)
                        .with("password", adminPassword))
                .retrieve()
                .bodyToMono(Map.class)
                .map(map -> (String) map.get("access_token"))
                .doOnSuccess(token -> log.debug("Admin token obtained successfully"))
                .doOnError(e -> log.error("Failed to get admin token: {}", e.getMessage()));
    }

    private Mono<Boolean> createUser(String adminToken, RegisterRequest request) {
        String usersUrl = keycloakUrl + "/admin/realms/" + realm + "/users";
        log.debug("Creating user at: {}", usersUrl);

        Map<String, Object> userRepresentation = Map.of(
                "username", request.getUsername(),
                "email", request.getEmail(),
                "firstName", request.getFirstName() != null ? request.getFirstName() : "",
                "lastName", request.getLastName() != null ? request.getLastName() : "",
                "enabled", true,
                "emailVerified", true,
                "credentials", List.of(Map.of(
                        "type", "password",
                        "value", request.getPassword(),
                        "temporary", false
                ))
        );

        return webClient.post()
                .uri(usersUrl)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRepresentation)
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    log.info("User creation response status: {}", response.getStatusCode());
                    return response.getStatusCode().is2xxSuccessful();
                })
                .doOnError(e -> log.error("Failed to create user: {}", e.getMessage()))
                .onErrorReturn(false);
    }

    @SuppressWarnings("unchecked")
    private TokenResponse mapToTokenResponse(Map<String, Object> map) {
        return TokenResponse.builder()
                .accessToken((String) map.get("access_token"))
                .refreshToken((String) map.get("refresh_token"))
                .tokenType((String) map.get("token_type"))
                .expiresIn(((Number) map.get("expires_in")).longValue())
                .scope((String) map.get("scope"))
                .build();
    }
}
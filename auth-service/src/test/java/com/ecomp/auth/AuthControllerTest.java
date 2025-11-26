package com.ecomp.auth;

import com.ecomp.auth.controller.AuthController;
import com.ecomp.auth.dto.AuthDtos.*;
import com.ecomp.auth.service.KeycloakAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private KeycloakAuthService authService;

    @InjectMocks
    private AuthController authController;

    private TokenResponse mockTokenResponse;
    private UserInfo mockUserInfo;

    @BeforeEach
    void setUp() {
        mockTokenResponse = TokenResponse.builder()
                .accessToken("test-access-token")
                .refreshToken("test-refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .scope("openid profile email")
                .build();

        mockUserInfo = UserInfo.builder()
                .id("user-123")
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .roles(List.of("CLIENT"))
                .build();
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("testuser", "password");
        when(authService.login(any(LoginRequest.class))).thenReturn(Mono.just(mockTokenResponse));

        Mono<ResponseEntity<TokenResponse>> result = authController.login(request);

        StepVerifier.create(result)
                .expectNextMatches(response -> 
                        response.getStatusCode() == HttpStatus.OK &&
                        response.getBody() != null &&
                        response.getBody().getAccessToken().equals("test-access-token"))
                .verifyComplete();
    }

    @Test
    void login_Failure() {
        LoginRequest request = new LoginRequest("testuser", "wrong-password");
        when(authService.login(any(LoginRequest.class))).thenReturn(Mono.error(new RuntimeException("Invalid credentials")));

        Mono<ResponseEntity<TokenResponse>> result = authController.login(request);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.UNAUTHORIZED)
                .verifyComplete();
    }

    @Test
    void register_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("password123")
                .firstName("New")
                .lastName("User")
                .role("CLIENT")
                .build();
        when(authService.register(any(RegisterRequest.class))).thenReturn(Mono.just(true));

        Mono<ResponseEntity<Object>> result = authController.register(request);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.CREATED)
                .verifyComplete();
    }

    @Test
    void register_Failure() {
        RegisterRequest request = RegisterRequest.builder()
                .username("existinguser")
                .email("existing@example.com")
                .password("password123")
                .build();
        when(authService.register(any(RegisterRequest.class))).thenReturn(Mono.just(false));

        Mono<ResponseEntity<Object>> result = authController.register(request);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();
    }

    @Test
    void refresh_Success() {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        when(authService.refreshToken(anyString())).thenReturn(Mono.just(mockTokenResponse));

        Mono<ResponseEntity<TokenResponse>> result = authController.refresh(request);

        StepVerifier.create(result)
                .expectNextMatches(response -> 
                        response.getStatusCode() == HttpStatus.OK &&
                        response.getBody() != null)
                .verifyComplete();
    }

    @Test
    void getUserInfo_Success() {
        when(authService.getUserInfo(anyString())).thenReturn(Mono.just(mockUserInfo));

        Mono<ResponseEntity<UserInfo>> result = authController.getUserInfo("Bearer test-token");

        StepVerifier.create(result)
                .expectNextMatches(response -> 
                        response.getStatusCode() == HttpStatus.OK &&
                        response.getBody() != null &&
                        response.getBody().getUsername().equals("testuser"))
                .verifyComplete();
    }

    @Test
    void logout_Success() {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        when(authService.logout(anyString())).thenReturn(Mono.empty());

        Mono<ResponseEntity<Void>> result = authController.logout(request);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK)
                .verifyComplete();
    }

    @Test
    void health_ReturnsOk() {
        ResponseEntity<String> result = authController.health();
        assert result.getStatusCode() == HttpStatus.OK;
        assert result.getBody() != null && result.getBody().contains("healthy");
    }
}

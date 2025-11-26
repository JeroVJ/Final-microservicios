package com.ecomp.auth.controller;

import com.ecomp.auth.dto.AuthDtos.*;
import com.ecomp.auth.service.KeycloakAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final KeycloakAuthService authService;

    @PostMapping("/login")
    public Mono<ResponseEntity<TokenResponse>> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());
        return authService.login(request)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Login failed: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
                });
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<Object>> register(@RequestBody RegisterRequest request) {
        log.info("Registration attempt for user: {}", request.getUsername());
        return authService.register(request)
                .map(success -> {
                    if (success) {
                        return ResponseEntity.status(HttpStatus.CREATED)
                                .body((Object) "Usuario registrado exitosamente");
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body((Object) "Error en el registro");
                    }
                })
                .onErrorResume(e -> {
                    log.error("Registration failed: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error: " + e.getMessage()));
                });
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<TokenResponse>> refresh(@RequestBody RefreshTokenRequest request) {
        log.info("Token refresh attempt");
        return authService.refreshToken(request.getRefreshToken())
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Token refresh failed: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
                });
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(@RequestBody RefreshTokenRequest request) {
        log.info("Logout attempt");
        return authService.logout(request.getRefreshToken())
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(e -> {
                    log.error("Logout failed: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @GetMapping("/userinfo")
    public Mono<ResponseEntity<UserInfo>> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        log.info("User info request");
        return authService.getUserInfo(token)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Get user info failed: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
                });
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is healthy");
    }
}

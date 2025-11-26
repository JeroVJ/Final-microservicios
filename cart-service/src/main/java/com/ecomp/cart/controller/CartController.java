package com.ecomp.cart.controller;

import com.ecomp.cart.dto.CartDtos.*;
import com.ecomp.cart.entity.Order;
import com.ecomp.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<List<CartItemDto>> getCart(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Getting cart for user: {}", userId);
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getCartTotal(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(cartService.getCartTotal(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemDto> addToCart(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String serviceId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        String userId = jwt.getSubject();
        log.info("Adding to cart: serviceId={}, quantity={}", serviceId, quantity);
        
        CartItemDto item = cartService.addToCart(userId, UUID.fromString(serviceId), quantity);
        return ResponseEntity.ok(item);
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartItemDto> updateQuantity(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String itemId,
            @RequestParam Integer quantity) {
        String userId = jwt.getSubject();
        log.info("Updating item {} quantity to {}", itemId, quantity);
        
        return cartService.updateQuantity(userId, UUID.fromString(itemId), quantity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeFromCart(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String itemId) {
        String userId = jwt.getSubject();
        log.info("Removing item {} from cart", itemId);
        
        if (cartService.removeFromCart(userId, UUID.fromString(itemId))) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Clearing cart for user: {}", userId);
        
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Checkout for user: {}", userId);
        
        try {
            Order order = cartService.checkout(userId);
            return ResponseEntity.ok(order);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getOrderHistory(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(cartService.getOrderHistory(userId));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Cart service is healthy");
    }
}

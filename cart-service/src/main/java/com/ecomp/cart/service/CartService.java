package com.ecomp.cart.service;

import com.ecomp.cart.client.CatalogClient;
import com.ecomp.cart.dto.CartDtos.*;
import com.ecomp.cart.entity.CartItem;
import com.ecomp.cart.entity.Order;
import com.ecomp.cart.entity.OrderItem;
import com.ecomp.cart.repository.CartItemRepository;
import com.ecomp.cart.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartItemRepository cartRepository;
    private final OrderRepository orderRepository;
    private final CatalogClient catalogClient;

    @Transactional(readOnly = true)
    public List<CartItemDto> getCart(String userId) {
        log.info("Getting cart for user: {}", userId);
        return cartRepository.findByUserId(userId).stream()
                .map(CartItemDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BigDecimal getCartTotal(String userId) {
        return cartRepository.calculateTotal(userId);
    }

    @Transactional
    public CartItemDto addToCart(String userId, UUID serviceId, int quantity) {
        log.info("Adding service {} to cart for user {}, quantity: {}", serviceId, userId, quantity);
        
        // Check if item already exists
        Optional<CartItem> existing = cartRepository.findByUserIdAndServiceId(userId, serviceId);
        
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            return CartItemDto.fromEntity(cartRepository.save(item));
        }
        
        // Fetch service info from catalog
        ServiceInfo serviceInfo = catalogClient.getServiceInfo(serviceId.toString())
                .block();
        
        CartItem newItem = CartItem.builder()
                .userId(userId)
                .serviceId(serviceId)
                .serviceName(serviceInfo != null ? serviceInfo.getName() : "Service")
                .serviceCategory(serviceInfo != null ? serviceInfo.getCategory() : null)
                .quantity(quantity)
                .unitPrice(serviceInfo != null ? serviceInfo.getPrice() : BigDecimal.ZERO)
                .build();
        
        return CartItemDto.fromEntity(cartRepository.save(newItem));
    }

    @Transactional
    public Optional<CartItemDto> updateQuantity(String userId, UUID itemId, int quantity) {
        log.info("Updating quantity for item {} to {}", itemId, quantity);
        
        return cartRepository.findById(itemId)
                .filter(item -> item.getUserId().equals(userId))
                .map(item -> {
                    if (quantity <= 0) {
                        cartRepository.delete(item);
                        return null;
                    }
                    item.setQuantity(quantity);
                    return CartItemDto.fromEntity(cartRepository.save(item));
                });
    }

    @Transactional
    public boolean removeFromCart(String userId, UUID itemId) {
        log.info("Removing item {} from cart", itemId);
        
        return cartRepository.findById(itemId)
                .filter(item -> item.getUserId().equals(userId))
                .map(item -> {
                    cartRepository.delete(item);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public void clearCart(String userId) {
        log.info("Clearing cart for user: {}", userId);
        cartRepository.deleteByUserId(userId);
    }

    @Transactional
    public Order checkout(String userId) {
        log.info("Checkout for user: {}", userId);
        
        List<CartItem> cartItems = cartRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }
        
        BigDecimal total = cartItems.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Order order = Order.builder()
                .userId(userId)
                .totalAmount(total)
                .status(Order.OrderStatus.COMPLETED)
                .build();
        
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .serviceId(cartItem.getServiceId())
                    .serviceName(cartItem.getServiceName())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .build();
            order.addItem(orderItem);
        }
        
        Order savedOrder = orderRepository.save(order);
        
        // Clear cart after checkout
        cartRepository.deleteByUserId(userId);
        
        log.info("Order created: {} with total: {}", savedOrder.getId(), total);
        return savedOrder;
    }

    @Transactional(readOnly = true)
    public List<Order> getOrderHistory(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}

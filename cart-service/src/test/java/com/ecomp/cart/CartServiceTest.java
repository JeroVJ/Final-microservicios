package com.ecomp.cart;

import com.ecomp.cart.dto.CartDtos.*;
import com.ecomp.cart.entity.CartItem;
import com.ecomp.cart.entity.Order;
import com.ecomp.cart.repository.CartItemRepository;
import com.ecomp.cart.repository.OrderRepository;
import com.ecomp.cart.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import com.ecomp.cart.client.CatalogClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private CatalogClient catalogClient;

    private static final String TEST_USER_ID = "user-123";
    private static final UUID TEST_SERVICE_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();
        orderRepository.deleteAll();
        
        // Mock catalog client
        when(catalogClient.getServiceInfo(anyString()))
                .thenReturn(Mono.just(ServiceInfo.builder()
                        .id(TEST_SERVICE_ID.toString())
                        .name("Test Service")
                        .category("Alojamiento")
                        .price(BigDecimal.valueOf(100.00))
                        .build()));
    }

    @Test
    void addToCart_NewItem_Success() {
        CartItemDto result = cartService.addToCart(TEST_USER_ID, TEST_SERVICE_ID, 2);

        assertNotNull(result);
        assertEquals(2, result.getQuantity());
        assertEquals("Test Service", result.getServiceName());
        assertEquals(BigDecimal.valueOf(100.00), result.getUnitPrice());
    }

    @Test
    void addToCart_ExistingItem_IncreasesQuantity() {
        cartService.addToCart(TEST_USER_ID, TEST_SERVICE_ID, 1);
        CartItemDto result = cartService.addToCart(TEST_USER_ID, TEST_SERVICE_ID, 2);

        assertEquals(3, result.getQuantity());
    }

    @Test
    void getCart_ReturnsUserItems() {
        cartRepository.save(CartItem.builder()
                .userId(TEST_USER_ID)
                .serviceId(TEST_SERVICE_ID)
                .serviceName("Service 1")
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(50.00))
                .build());
        cartRepository.save(CartItem.builder()
                .userId("other-user")
                .serviceId(UUID.randomUUID())
                .serviceName("Other Service")
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(75.00))
                .build());

        List<CartItemDto> results = cartService.getCart(TEST_USER_ID);

        assertEquals(1, results.size());
        assertEquals("Service 1", results.get(0).getServiceName());
    }

    @Test
    void getCartTotal_CalculatesCorrectly() {
        cartRepository.save(CartItem.builder()
                .userId(TEST_USER_ID)
                .serviceId(UUID.randomUUID())
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(50.00))
                .build());
        cartRepository.save(CartItem.builder()
                .userId(TEST_USER_ID)
                .serviceId(UUID.randomUUID())
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(100.00))
                .build());

        BigDecimal total = cartService.getCartTotal(TEST_USER_ID);

        assertEquals(0, BigDecimal.valueOf(200.00).compareTo(total));
    }

    @Test
    void updateQuantity_Success() {
        CartItem item = cartRepository.save(CartItem.builder()
                .userId(TEST_USER_ID)
                .serviceId(TEST_SERVICE_ID)
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(50.00))
                .build());

        var result = cartService.updateQuantity(TEST_USER_ID, item.getId(), 5);

        assertTrue(result.isPresent());
        assertEquals(5, result.get().getQuantity());
    }

    @Test
    void updateQuantity_ZeroRemovesItem() {
        CartItem item = cartRepository.save(CartItem.builder()
                .userId(TEST_USER_ID)
                .serviceId(TEST_SERVICE_ID)
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(50.00))
                .build());

        cartService.updateQuantity(TEST_USER_ID, item.getId(), 0);

        assertFalse(cartRepository.existsById(item.getId()));
    }

    @Test
    void removeFromCart_Success() {
        CartItem item = cartRepository.save(CartItem.builder()
                .userId(TEST_USER_ID)
                .serviceId(TEST_SERVICE_ID)
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(50.00))
                .build());

        boolean result = cartService.removeFromCart(TEST_USER_ID, item.getId());

        assertTrue(result);
        assertFalse(cartRepository.existsById(item.getId()));
    }

    @Test
    void removeFromCart_WrongUser_Fails() {
        CartItem item = cartRepository.save(CartItem.builder()
                .userId(TEST_USER_ID)
                .serviceId(TEST_SERVICE_ID)
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(50.00))
                .build());

        boolean result = cartService.removeFromCart("wrong-user", item.getId());

        assertFalse(result);
        assertTrue(cartRepository.existsById(item.getId()));
    }

    @Test
    void clearCart_RemovesAllUserItems() {
        cartRepository.save(CartItem.builder()
                .userId(TEST_USER_ID)
                .serviceId(UUID.randomUUID())
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(50.00))
                .build());
        cartRepository.save(CartItem.builder()
                .userId(TEST_USER_ID)
                .serviceId(UUID.randomUUID())
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(75.00))
                .build());

        cartService.clearCart(TEST_USER_ID);

        assertTrue(cartRepository.findByUserId(TEST_USER_ID).isEmpty());
    }

    @Test
    void checkout_CreatesOrderAndClearsCart() {
        cartRepository.save(CartItem.builder()
                .userId(TEST_USER_ID)
                .serviceId(TEST_SERVICE_ID)
                .serviceName("Test Service")
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(50.00))
                .build());

        Order order = cartService.checkout(TEST_USER_ID);

        assertNotNull(order);
        assertEquals(Order.OrderStatus.COMPLETED, order.getStatus());
        assertEquals(0, BigDecimal.valueOf(100.00).compareTo(order.getTotalAmount()));
        assertEquals(1, order.getItems().size());
        assertTrue(cartRepository.findByUserId(TEST_USER_ID).isEmpty());
    }

    @Test
    void checkout_EmptyCart_ThrowsException() {
        assertThrows(IllegalStateException.class, () -> {
            cartService.checkout(TEST_USER_ID);
        });
    }
}

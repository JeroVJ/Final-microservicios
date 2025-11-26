package com.ecomp.cart.repository;

import com.ecomp.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    
    List<CartItem> findByUserId(String userId);
    
    Optional<CartItem> findByUserIdAndServiceId(String userId, UUID serviceId);
    
    void deleteByUserId(String userId);
    
    @Query("SELECT COALESCE(SUM(c.unitPrice * c.quantity), 0) FROM CartItem c WHERE c.userId = :userId")
    BigDecimal calculateTotal(String userId);
    
    boolean existsByUserIdAndServiceId(String userId, UUID serviceId);
}

package com.ecomp.cart.dto;

import com.ecomp.cart.entity.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class CartDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemDto {
        private String id;
        private String serviceId;
        private String serviceName;
        private String serviceCategory;
        private Integer quantity;
        private BigDecimal unitPrice;

        public static CartItemDto fromEntity(CartItem entity) {
            if (entity == null) return null;
            return CartItemDto.builder()
                    .id(entity.getId().toString())
                    .serviceId(entity.getServiceId().toString())
                    .serviceName(entity.getServiceName())
                    .serviceCategory(entity.getServiceCategory())
                    .quantity(entity.getQuantity())
                    .unitPrice(entity.getUnitPrice())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceInfo {
        private String id;
        private String name;
        private String category;
        private BigDecimal price;
    }
}

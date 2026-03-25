package com.jitendra.inventoryservice.mapper;

import com.jitendra.inventoryservice.dto.*;
import com.jitendra.inventoryservice.model.Inventory;

public class InventoryMapper {

    public static Inventory toEntity(InventoryRequestDto dto) {
        if (dto == null) return null;

        return new Inventory(
                dto.getProductId(),
                dto.getQuantity(),
                dto.getQuantity() > 0
        );
    }

    public static InventoryResponseDto toDto(Inventory inventory) {
        if (inventory == null) return null;

        return InventoryResponseDto.builder()
                .productId(inventory.getProductId())
                .quantity(inventory.getQuantity())
                .inStock(inventory.getInStock())
                .build();
    }
}